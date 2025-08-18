import os, hashlib, re
from typing import List, Optional, Dict, Tuple
from fastapi import FastAPI, Response, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from langdetect import detect
from functools import lru_cache
import torch

try:
    import redis  # type: ignore
except Exception:
    redis = None

from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

# =========================
# Config
# =========================
DEFAULT_MODEL_ID = os.getenv("MODEL_ID", "facebook/m2m100_418M")
REDIS_URL = os.getenv("REDIS_URL", "redis://localhost:6379/0")
CACHE_TTL = int(os.getenv("CACHE_TTL_SECONDS", "604800"))  # 7 days
MARIAN_OFF = os.getenv("MARIAN_OFF", "0") == "1"
NLLB_FORCE_WORDS = os.getenv("NLLB_FORCE_WORDS", "0") == "1"  # 기본 False 권장
DEBUG = os.getenv("DEBUG", "0") == "1"
WARMUP_TEXT = os.getenv("WARMUP_TEXT", "warm up")
MAX_CHARS = int(os.getenv("MAX_CHARS", "1000"))

# 표준화
LANG_MAP = {"zh-cn": "zh", "zh-tw": "zh", "pt-br": "pt"}

# 자주 쓰는 언어쌍 라우팅(Marian)
MARIAN_ROUTES: Dict[Tuple[str, str], str] = {
    ("ko", "en"): "Helsinki-NLP/opus-mt-ko-en",
    ("en", "ko"): "Helsinki-NLP/opus-mt-en-ko",
    ("ja", "en"): "Helsinki-NLP/opus-mt-ja-en",
    ("en", "ja"): "Helsinki-NLP/opus-mt-en-ja",
    ("zh", "en"): "Helsinki-NLP/opus-mt-zh-en",
    ("en", "zh"): "Helsinki-NLP/opus-mt-en-zh",
    # 한국어 풀연결 + 중↔일
    ("ko", "ja"): "Helsinki-NLP/opus-mt-ko-ja",
    ("ja", "ko"): "Helsinki-NLP/opus-mt-ja-ko",
    ("ko", "zh"): "Helsinki-NLP/opus-mt-ko-zh",
    ("zh", "ko"): "Helsinki-NLP/opus-mt-zh-ko",
    ("ja", "zh"): "Helsinki-NLP/opus-mt-ja-zh",
    ("zh", "ja"): "Helsinki-NLP/opus-mt-zh-ja",
}

# =========================
# Idol Glossary (확장 가능)
# =========================
# 타겟 언어별 표기 통일(후처리)
TGT_GLOSSARY: Dict[str, Dict[str, str]] = {
    "en": {
        "응원봉": "Lightstick",
        "라이트스틱": "Lightstick",
        "봉": "Lightstick",
        "light stick": "Lightstick",
        "light-stick": "Lightstick",
        "cheering stick": "Lightstick",
        "support bar": "Lightstick",
        "fan stick": "Lightstick",
        "직캠": "fancam",
        "무대 직캠": "fancam",
        "무대직캠": "fancam",
        "최애": "bias",
        "차애": "bias wrecker",
        "바이어스 레커": "bias wrecker",
        "본진": "main group",
        "원픽": "bias",
        "막내": "maknae",
        "맏형": "oldest member",
        "리더": "leader",
        "메인보컬": "main vocalist",
        "메보": "main vocalist",
        "리드보컬": "lead vocalist",
        "서브보컬": "sub vocalist",
        "메인댄서": "main dancer",
        "메댄": "main dancer",
        "리드댄서": "lead dancer",
        "메인래퍼": "main rapper",
        "메랩": "main rapper",
        "리드래퍼": "lead rapper",
        "비주얼": "visual",
        "센터": "center",
        "컴백": "comeback",
        "티저": "teaser",
        "선공개": "pre-release",
        "활동기": "promotions",
        "완전체": "full group",
        "유닛": "unit",
        "솔로": "solo",
        "사녹": "pre-recording",
        "음방": "music show",
        "인기가요": "Inkigayo",
        "뮤직뱅크": "Music Bank",
        "엠카": "M Countdown",
        "쇼챔": "Show Champion",
        "입덕": "become a fan",
        "입덕영상": "starter guide",
        "덕질": "stanning",
        "팬싸": "fansign",
        "팬사인회": "fansign",
        "팬미팅": "fan meeting",
        "팬덤": "fandom",
        "팬카페": "fan cafe",
        "공카": "official fan cafe",
        "팬아트": "fan art",
        "팬픽": "fanfic",
        "셀카": "selfie",
        "공식 굿즈": "official merch",
        "굿즈": "merch",
        "랜덤 포카": "random photocard",
        "포카": "photocard",
        "포카바인더": "photocard binder",
        "응원법": "fanchant",
        "챈트": "fanchant",
        "스밍": "streaming",
        "총공": "mass streaming",
        "투표": "voting",
        "타이틀곡": "title track",
        "수록곡": "b-side",
        "음반": "album",
        "앨범": "album",
        "음원차트": "digital chart",
    },
    "ko": {
        "Lightstick": "응원봉",
        "lightstick": "응원봉",
        "Lightsticks": "응원봉",
        "lightsticks": "응원봉",
        "light sticks": "응원봉",
        "light-sticks": "응원봉",
        "cheering stick": "응원봉",
        "cheering sticks": "응원봉",
        "fancam": "직캠",
        "Bias": "최애",
        "bias": "최애",
        "biases": "최애",
        "Bias Wrecker": "차애",
        "bias wrecker": "차애",
        "main group": "본진",
        "maknae": "막내",
        "oldest member": "맏형",
        "leader": "리더",
        "main vocalist": "메인보컬",
        "lead vocalist": "리드보컬",
        "sub vocalist": "서브보컬",
        "main dancer": "메인댄서",
        "lead dancer": "리드댄서",
        "main rapper": "메인래퍼",
        "lead rapper": "리드래퍼",
        "visual": "비주얼",
        "center": "센터",
        "comeback": "컴백",
        "teaser": "티저",
        "pre-release": "선공개",
        "promotions": "활동기",
        "full group": "완전체",
        "unit": "유닛",
        "solo": "솔로",
        "pre-recording": "사녹",
        "music show": "음방",
        "Inkigayo": "인기가요",
        "Music Bank": "뮤직뱅크",
        "M Countdown": "엠카",
        "Show Champion": "쇼챔",
        "become a fan": "입덕",
        "starter guide": "입덕영상",
        "stanning": "덕질",
        "fansign": "팬싸",
        "fan meeting": "팬미팅",
        "fandom": "팬덤",
        "fan cafe": "팬카페",
        "official fan cafe": "공카",
        "fan art": "팬아트",
        "fanfic": "팬픽",
        "selfie": "셀카",
        "official merch": "공식 굿즈",
        "merch": "굿즈",
        "random photocard": "랜덤 포카",
        "photocard": "포카",
        "photocard binder": "포카바인더",
        "fanchant": "응원법",
        "fan chant": "응원법",
        "fan chants": "응원법",
        "streaming": "스밍",
        "mass streaming": "총공",
        "voting": "투표",
        "title track": "타이틀곡",
        "b-side": "수록곡",
        "album": "앨범",
        "digital chart": "음원차트",
    },
    "ja": {
        "ペンライト": "ライトスティック",
        "推し": "推し",
        "箱推し": "箱推し",
        "直撮り": "ファンカム",
    },
}

# 소스→타겟 용어 강제(소스 힌트/force_words용)
SRC2TGT_GLOSSARY: Dict[str, Dict[str, Dict[str, str]]] = {
    "ko": {
        "en": {
            "응원봉": "Lightstick",
            "라이트스틱": "Lightstick",
            "무대 직캠": "fancam",
            "직캠": "fancam",
            "최애": "bias",
            "차애": "bias wrecker",
            "바이어스 레커": "bias wrecker",
            "본진": "main group",
            "원픽": "bias",
            "막내": "maknae",
            "맏형": "oldest member",
            "리더": "leader",
            "메인보컬": "main vocalist",
            "메보": "main vocalist",
            "리드보컬": "lead vocalist",
            "서브보컬": "sub vocalist",
            "메인댄서": "main dancer",
            "메댄": "main dancer",
            "리드댄서": "lead dancer",
            "메인래퍼": "main rapper",
            "메랩": "main rapper",
            "리드래퍼": "lead rapper",
            "비주얼": "visual",
            "센터": "center",
            "컴백": "comeback",
            "티저": "teaser",
            "선공개": "pre-release",
            "활동기": "promotions",
            "완전체": "full group",
            "유닛": "unit",
            "솔로": "solo",
            "사녹": "pre-recording",
            "음방": "music show",
            "인기가요": "Inkigayo",
            "뮤직뱅크": "Music Bank",
            "엠카": "M Countdown",
            "쇼챔": "Show Champion",
            "입덕": "become a fan",
            "입덕영상": "starter guide",
            "덕질": "stanning",
            "팬싸": "fansign",
            "팬사인회": "fansign",
            "팬미팅": "fan meeting",
            "팬덤": "fandom",
            "팬카페": "fan cafe",
            "공카": "official fan cafe",
            "팬아트": "fan art",
            "팬픽": "fanfic",
            "셀카": "selfie",
            "공식 굿즈": "official merch",
            "굿즈": "merch",
            "랜덤 포카": "random photocard",
            "포카": "photocard",
            "포카바인더": "photocard binder",
            "응원법": "fanchant",
            "스밍": "streaming",
            "총공": "mass streaming",
            "투표": "voting",
            "타이틀곡": "title track",
            "수록곡": "b-side",
            "앨범": "album",
            "음원차트": "digital chart",
        },
        "ja": {
            "응원봉": "ライトスティック",
            "직캠": "ファンカム",
            "최애": "推し",
            "차애": "沼落ち要員",
        }
    },
    "en": {
        "ko": {
            "Lightstick": "응원봉",
            "lightstick": "응원봉",
            "Lightsticks": "응원봉",
            "lightsticks": "응원봉",
            "light sticks": "응원봉",
            "light-sticks": "응원봉",
            "cheering stick": "응원봉",
            "cheering sticks": "응원봉",
            "fancam": "직캠",
            "bias": "최애",
            "biases": "최애",
            "Bias": "최애",
            "Bias Wrecker": "차애",
            "bias wrecker": "차애",
            "main group": "본진",
            "maknae": "막내",
            "oldest member": "맏형",
            "leader": "리더",
            "main vocalist": "메인보컬",
            "lead vocalist": "리드보컬",
            "sub vocalist": "서브보컬",
            "main dancer": "메인댄서",
            "lead dancer": "리드댄서",
            "main rapper": "메인래퍼",
            "lead rapper": "리드래퍼",
            "visual": "비주얼",
            "center": "센터",
            "comeback": "컴백",
            "teaser": "티저",
            "pre-release": "선공개",
            "promotions": "활동기",
            "full group": "완전체",
            "unit": "유닛",
            "solo": "솔로",
            "pre-recording": "사녹",
            "music show": "음방",
            "Inkigayo": "인기가요",
            "Music Bank": "뮤직뱅크",
            "M Countdown": "엠카",
            "Show Champion": "쇼챔",
            "become a fan": "입덕",
            "starter guide": "입덕영상",
            "stanning": "덕질",
            "fansign": "팬싸",
            "fan meeting": "팬미팅",
            "fandom": "팬덤",
            "fan cafe": "팬카페",
            "official fan cafe": "공카",
            "fan art": "팬아트",
            "fanfic": "팬픽",
            "selfie": "셀카",
            "official merch": "공식 굿즈",
            "merch": "굿즈",
            "random photocard": "랜덤 포카",
            "photocard": "포카",
            "photocard binder": "포카바인더",
            "fanchant": "응원법",
            "fan chant": "응원법",
            "fan chants": "응원법",
            "streaming": "스밍",
            "mass streaming": "총공",
            "voting": "투표",
            "title track": "타이틀곡",
            "b-side": "수록곡",
            "album": "앨범",
            "digital chart": "음원차트",
        }
    },
    "ja": {
        "en": {
            "ペンライト": "Lightstick",
            "推し": "bias",
            "箱推し": "group stan",
            "直撮り": "fancam",
        }
    }
}

# ko->en에서 '응원봉' 오역 휴리스틱
KO_EN_COMMON_MISREADS = (
    r"\bhelmet\b",
    r"\bsupport\s*bar\b",
    r"\bcheering\s*stick\b",
    r"\bbaton\b",
    r"\blight\s*stick\b",
)

# =========================
# App / Redis / Model cache
# =========================
app = FastAPI(title="Duck-On Translate Service")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], allow_credentials=True,
    allow_methods=["*"], allow_headers=["*"],
)

rds = None
if redis is not None:
    try:
        rds = redis.from_url(REDIS_URL); rds.ping()
    except Exception:
        rds = None

_model_cache: Dict[str, Dict] = {}
torch.set_grad_enabled(False)

# =========================
# Helpers
# =========================
def _has_latin(s: str) -> bool:
    return bool(re.search(r'[A-Za-z]', s or ""))

def _compile_kv_patterns(table: Dict[str, str]):
    items = sorted(table.items(), key=lambda kv: len(kv[0]), reverse=True)
    compiled = []
    for k, v in items:
        if _has_latin(k):
            pat = re.compile(rf'\b{re.escape(k)}\b', re.IGNORECASE)
        else:
            pat = re.compile(re.escape(k))
        compiled.append((pat, v))
    return compiled

@lru_cache(maxsize=128)
def _get_tgt_glossary_patterns(tgt_lang: str):
    return _compile_kv_patterns(TGT_GLOSSARY.get(tgt_lang or "", {}) or {})

@lru_cache(maxsize=512)
def _get_src2tgt_patterns(src_lang: str, tgt_lang: str):
    table = SRC2TGT_GLOSSARY.get(src_lang or "", {}).get(tgt_lang or "", {}) or {}
    return _compile_kv_patterns(table)

def split_sentences(text: str) -> List[str]:
    t = (text or "").strip()
    if not t:
        return []
    seps = r'([\.!\?]+|[。！？…]+)'
    parts = re.split(seps, t)
    chunks, buf = [], ""
    for p in parts:
        if not p:
            continue
        buf += p
        if re.fullmatch(seps, p):
            chunks.append(buf.strip()); buf = ""
    if buf.strip():
        chunks.append(buf.strip())
    merged = []
    for c in chunks:
        if merged and len(c) < 3:
            merged[-1] = (merged[-1] + " " + c).strip()
        else:
            merged.append(c)
    return merged

def norm_lang(lg: Optional[str]) -> Optional[str]:
    if not lg: return None
    x = lg.lower()
    return LANG_MAP.get(x, x)

def guess_lang(text: str) -> str:
    try:
        lg = detect(text)
        return norm_lang(lg) or "en"
    except Exception:
        return "en"

def cache_key(text: str, src: Optional[str], tgt: str, route_id: str, gloss_ver: str="v7") -> str:
    h = hashlib.sha1(text.encode("utf-8")).hexdigest()
    return f"mt:{route_id}:{h}:{src or 'auto'}:{tgt}:{gloss_ver}"

def is_m2m(model_type: str) -> bool:
    return model_type.lower() in ("m2m_100", "m2m100")

def is_nllb(tokenizer) -> bool:
    return bool(
        hasattr(tokenizer, "lang_code_to_id") or
        any(s in getattr(tokenizer, "all_special_tokens", []) for s in ("eng_Latn", "<2eng_Latn>"))
    )

def to_nllb_code(short: str) -> str:
    short = (short or "en").lower()
    m = {
        "en": "eng_Latn", "ko": "kor_Hang", "ja": "jpn_Jpan",
        "zh": "zho_Hans", "zh-cn": "zho_Hans", "zh-tw": "zho_Hant",
        "fr": "fra_Latn", "de": "deu_Latn", "es": "spa_Latn", "pt": "por_Latn",
        "ru": "rus_Cyrl", "vi": "vie_Latn", "th": "tha_Thai", "id": "ind_Latn",
        "tr": "tur_Latn",
    }
    return m.get(short, "eng_Latn")

def get_nllb_lang_id(tokenizer, code: str) -> Optional[int]:
    if hasattr(tokenizer, "lang_code_to_id") and isinstance(tokenizer.lang_code_to_id, dict):
        tid = tokenizer.lang_code_to_id.get(code)
        if isinstance(tid, int) and tid >= 0:
            return tid
    for c in (code, f"<2{code}>", f"<<{code}>>", f"__{code}__"):
        try:
            tid = tokenizer.convert_tokens_to_ids(c)
            if isinstance(tid, int) and tid >= 0 and tid != getattr(tokenizer, "unk_token_id", -1):
                return tid
        except Exception:
            pass
    return None

# -------- Patched: load_model with HF token + safe fallback --------
def load_model(model_id: str):
    if model_id in _model_cache:
        p = _model_cache[model_id]
        return p["tok"], p["mdl"], p["type"]

    hf_token = os.getenv("HF_TOKEN") or os.getenv("HUGGINGFACE_HUB_TOKEN") or None

    def _really_load(mid: str):
        tok = AutoTokenizer.from_pretrained(mid, token=hf_token)
        try:
            mdl = AutoModelForSeq2SeqLM.from_pretrained(
                mid,
                token=hf_token,
                torch_dtype=torch.float16,
                device_map="auto",
            )
        except Exception:
            mdl = AutoModelForSeq2SeqLM.from_pretrained(mid, token=hf_token)
        mdl.eval()
        mtype = getattr(mdl.config, "model_type", "")
        _model_cache[mid] = {"tok": tok, "mdl": mdl, "type": mtype}
        return tok, mdl, mtype

    try:
        return _really_load(model_id)
    except Exception as e:
        if model_id != DEFAULT_MODEL_ID:
            if DEBUG:
                print(f"[WARN] load_model('{model_id}') failed → fallback '{DEFAULT_MODEL_ID}': {e}")
            try:
                return _really_load(DEFAULT_MODEL_ID)
            except Exception as e2:
                if DEBUG:
                    print(f"[ERROR] fallback '{DEFAULT_MODEL_ID}' failed: {e2}")
                raise
        raise

def pick_model(src: Optional[str], tgt: str) -> str:
    if MARIAN_OFF:
        return DEFAULT_MODEL_ID
    s = norm_lang(src) if src else None
    t = norm_lang(tgt) or "en"
    if s and (s, t) in MARIAN_ROUTES:
        return MARIAN_ROUTES[(s, t)]
    return DEFAULT_MODEL_ID

def build_force_words(tokenizer, tgt_lang: str, src_text: str, src_lang: Optional[str]):
    s = norm_lang(src_lang) if src_lang else guess_lang(src_text)
    t = norm_lang(tgt_lang) or "en"
    phrases = []
    table = SRC2TGT_GLOSSARY.get(s, {}).get(t, {})
    for _, target_phrase in table.items():
        if not target_phrase: continue
        ids = tokenizer.encode(target_phrase, add_special_tokens=False)
        if ids: phrases.append(ids)
    return phrases

# ---- Fast path: only force words actually appearing in source, cap to 3 ----
def build_force_words_present(tokenizer, tgt_lang: str, src_text: str, src_lang: Optional[str]):
    s = norm_lang(src_lang) if src_lang else guess_lang(src_text)
    t = norm_lang(tgt_lang) or "en"
    table = SRC2TGT_GLOSSARY.get(s, {}).get(t, {}) or {}
    lowersrc = (src_text or "").lower()
    out = []
    cnt = 0
    for src_phrase, target_phrase in table.items():
        if not target_phrase:
            continue
        if (src_phrase or "").lower() in lowersrc:
            ids = tokenizer.encode(target_phrase, add_special_tokens=False)
            if ids:
                out.append(ids)
                cnt += 1
                if cnt >= 3:  # cap to 3
                    break
    return out

def apply_src_hint(text: str, src_lang: Optional[str], tgt_lang: str) -> str:
    s = norm_lang(src_lang) if src_lang else guess_lang(text)
    t = norm_lang(tgt_lang) or "en"
    for pat, rep in _get_src2tgt_patterns(s, t):
        text = pat.sub(rep, text)
    return text

def apply_tgt_glossary(text: str, tgt_lang: str) -> str:
    for pat, rep in _get_tgt_glossary_patterns(norm_lang(tgt_lang) or ""):
        text = pat.sub(rep, text)
    return text

def _ids_safe(tokenizer, model):
    eos = getattr(tokenizer, "eos_token_id", None) or getattr(model.config, "eos_token_id", None)
    pad = getattr(tokenizer, "pad_token_id", None) or getattr(model.config, "pad_token_id", None) or eos
    return eos, pad

def _guard_text_len(text: str):
    if text is None:
        raise HTTPException(status_code=400, detail="text is required")
    if len(text) > MAX_CHARS:
        raise HTTPException(status_code=413, detail=f"text too long (>{MAX_CHARS} chars)")

# =========================
# Schemas
# =========================
class TranslateItem(BaseModel):
    text: str
    src: Optional[str] = None
    tgt: str
    use_glossary: bool = True

class TranslateBatch(BaseModel):
    items: List[TranslateItem]

# =========================
# Core translate
# =========================
def translate_core(item: TranslateItem) -> Tuple[str, Dict[str, str]]:
    _guard_text_len(item.text)
    text = item.text
    src = norm_lang(item.src) if item.src else None
    tgt = norm_lang(item.tgt) or "en"

    route_model_id = pick_model(src, tgt)
    tokenizer, model, model_type = load_model(route_model_id)

    key = cache_key(text, src, tgt, route_model_id)
    if rds:
        cached = rds.get(key)
        if cached:
            out = cached.decode("utf-8")
            return out, {"cache": "HIT", "model": route_model_id}

    use_m2m = is_m2m(model_type)
    nllb = is_nllb(tokenizer)

    bos_id = None
    if nllb:
        src_code = to_nllb_code(src or guess_lang(text))
        tgt_code = to_nllb_code(tgt)
        try:
            if hasattr(tokenizer, "set_src_lang"):
                tokenizer.set_src_lang(src_code)
            else:
                tokenizer.src_lang = src_code
        except Exception:
            pass
        bos_id = get_nllb_lang_id(tokenizer, tgt_code)
    elif use_m2m and hasattr(tokenizer, "src_lang"):
        tokenizer.src_lang = src or guess_lang(text)

    eos_id, pad_id = _ids_safe(tokenizer, model)
    base_gen = dict(
        max_new_tokens=96,
        min_new_tokens=4,
        num_beams=3,
        no_repeat_ngram_size=3,
        length_penalty=1.0,
        repetition_penalty=1.05,
        early_stopping=True,
        eos_token_id=eos_id,
        pad_token_id=pad_id,
    )

    # 🔧 영어→한국어일 때는 소스치환을 끄고(강제어/후처리만 사용)
    src_effective = src or guess_lang(text)
    use_hint = (item.use_glossary and not (src_effective == "en" and tgt == "ko"))
    pre_text = apply_src_hint(text, src, tgt) if use_hint else text
    segments = split_sentences(pre_text) or [pre_text]

    outs = []
    for seg in segments:
        inputs = tokenizer(seg, return_tensors="pt")
        if next(model.parameters()).is_cuda:
            inputs = {k: v.to(model.device) for k, v in inputs.items()}

        gen_kwargs = dict(base_gen)

        # 🔧 문장별 강제어 적용 (세그먼트 안에 등장할 때만)
        if item.use_glossary and (not nllb or NLLB_FORCE_WORDS):
            fwords = build_force_words_present(tokenizer, tgt, seg, src)
            if fwords:
                gen_kwargs["force_words_ids"] = fwords
                gen_kwargs["num_beams"] = max(4, min(6, 2 + len(fwords)))

        try:
            outputs = model.generate(**inputs, **gen_kwargs)
        except Exception:
            # 강제어로 인한 실패는 "해당 문장만" 강제어 제거 후 재시도
            gen_kwargs.pop("force_words_ids", None)
            gen_kwargs["num_beams"] = 3
            outputs = model.generate(**inputs, **gen_kwargs)

        piece = tokenizer.batch_decode(outputs, skip_special_tokens=True)[0]
        outs.append(piece)

    out = " ".join(outs).strip()

    # ko->en 휴리스틱 (응원봉)
    if (src or guess_lang(text)) == "ko" and tgt == "en":
        if "응원봉" in text and "Lightstick" not in out:
            for pat in KO_EN_COMMON_MISREADS:
                out = re.sub(pat, "Lightstick", out, flags=re.IGNORECASE)

    if item.use_glossary:
        out = apply_tgt_glossary(out, tgt)

    if rds:
        rds.setex(key, CACHE_TTL, out)

    return out, {"cache": "MISS", "model": route_model_id}

def translate_once(item: TranslateItem) -> str:
    out, _ = translate_core(item)
    return out

# metrics (optional)
try:
    from prometheus_fastapi_instrumentator import Instrumentator
    _ENABLE_METRICS = True
except Exception:
    _ENABLE_METRICS = False

@app.on_event("startup")
def _metrics_expose():
    if _ENABLE_METRICS:
        Instrumentator().instrument(app).expose(app, endpoint="/metrics")

@app.on_event("startup")
def _warmup_and_mark_ready():
    try:
        tok, mdl, _ = load_model(DEFAULT_MODEL_ID)
        inputs = tok(WARMUP_TEXT, return_tensors="pt")
        if next(mdl.parameters()).is_cuda:
            inputs = {k: v.to(mdl.device) for k, v in inputs.items()}
        _ = mdl.generate(**inputs, max_new_tokens=4)
        app.state.ready = True
    except Exception:
        app.state.ready = False

@app.get("/ready")
def ready():
    return {"ready": bool(getattr(app.state, "ready", False))}

# =========================
# Routes
# =========================
@app.get("/health")
def health():
    status = {}
    for mid, payload in _model_cache.items():
        dev = "cuda" if next(payload["mdl"].parameters()).is_cuda else "cpu"
        status[mid] = {"device": dev, "type": payload["type"]}
    return {"ok": True, "default_model": DEFAULT_MODEL_ID, "loaded": status}

class TranslateItemPublic(BaseModel):
    # 참고: 이 서버를 직접 칠 때는 text/src/tgt 사용.
    text: str
    src: Optional[str] = None
    tgt: str
    use_glossary: bool = True

@app.post("/translate")
def translate(req: TranslateItemPublic, response: Response):
    out, meta = translate_core(req)
    response.headers["X-Translate-Model"] = meta.get("model", "")
    response.headers["X-Cache"] = meta.get("cache", "")
    response.headers["X-Ready"] = "1" if bool(getattr(app.state, "ready", False)) else "0"
    return {"translation": out}

class TranslateBatch(BaseModel):
    items: List[TranslateItemPublic]

@app.post("/translate/batch")
def translate_batch(req: TranslateBatch, response: Response):
    outs = []
    cache_hits = 0
    model_used = ""
    for it in req.items:
        out, meta = translate_core(it)
        outs.append(out)
        model_used = model_used or meta.get("model", "")
        cache_hits += 1 if meta.get("cache") == "HIT" else 0
    response.headers["X-Translate-Model"] = model_used
    response.headers["X-Cache-Hits"] = str(cache_hits)
    response.headers["X-Ready"] = "1" if bool(getattr(app.state, "ready", False)) else "0"
    return {"translations": outs}