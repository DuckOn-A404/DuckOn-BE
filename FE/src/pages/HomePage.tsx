import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import VideoCard from "../components/domain/video/VideoCard";
import ArtistCard from "../components/domain/artist/ArtistCard";
import { getRandomArtists } from "../api/artistService";
import { type Artist } from "../types/artist";
import { useTrendingRooms } from "../hooks/useTrendingRooms";
import VideoCardSkeleton from "../components/domain/video/VideoCardSkeleton";
import ArtistCardSkeleton from "../components/domain/artist/ArtistCartdSekeleton";
import { Tv, HelpCircle } from "lucide-react";
import GuideModal, {
  type GuideStep,
} from "../components/common/modal/GuideModal";
import { createSlug } from "../utils/slugUtils";

const HomePage = () => {
  const [recommendedArtists, setRecommendedArtists] = useState<Artist[]>([]);
  const [isLoadingArtists, setIsLoadingArtists] = useState(true);
  const navigate = useNavigate();

  const {
    data: trendingRooms,
    isLoading: isLoadingTrending,
    error: trendingError,
  } = useTrendingRooms(1, 9);

  const handleCardClick = (artistId: number, nameEn: string) => {
    const slug = createSlug(nameEn);

    navigate(`/artist/${slug}`, {
      state: { artistId: artistId },
    });
  };

  useEffect(() => {
    const fetchRandomArtists = async () => {
      try {
        const data = await getRandomArtists(5);
        setRecommendedArtists(data);
      } catch (error) {
        console.error("추천 아티스트를 불러오는 데 실패했습니다.", error);
        setRecommendedArtists([]);
      } finally {
        setIsLoadingArtists(false);
      }
    };
    fetchRandomArtists();
  }, []);

  const [guideOpen, setGuideOpen] = useState(false);
  const [guideIndex, setGuideIndex] = useState(0);

  const guideSteps: GuideStep[] = [
    {
      title: "아티스트 검색 & 팔로우",
      desc:
        "아티스트 목록에서 좋아하는 아티스트를 찾아 [팔로우]를 눌러주세요. " +
        "팔로우하면 전용 채팅과 라이브 알림을 받을 수 있어요.",
      img: "/guide/follow.png",
      alt: "아티스트 상세에서 팔로우 버튼 위치",
    },
    {
      title: "새 방 만들기",
      desc:
        "아티스트 페이지의 [새 방 만들기] 버튼을 눌러 방을 생성할 수 있어요. " +
        "팔로우 중인 아티스트에서만 방 생성이 가능해요.",
      img: "/guide/create-room.png",
      alt: "아티스트 페이지의 새 방 만들기 버튼 위치",
    },
    {
      title: "유튜브 URL & 입장 설정",
      desc:
        "방 제목과 유튜브 URL을 입력하고, 필요하다면 비밀번호/입장 질문을 설정하세요. " +
        "완료하면 팬들과 함께 실시간으로 즐길 준비 끝!",
      img: "/guide/room-form.png",
      alt: "방 만들기 폼 작성 화면",
    },
  ];

  const openGuide = (i = 0) => {
    setGuideIndex(i);
    setGuideOpen(true);
  };
  const nextGuide = () => setGuideIndex((i) => (i + 1) % guideSteps.length);
  const prevGuide = () =>
    setGuideIndex((i) => (i - 1 + guideSteps.length) % guideSteps.length);

  // ---------------------------------------------------

  return (
    <div>
      {/* 랜딩(Hero) 섹션 */}
      <div
        className="relative w-full h-96 bg-cover bg-center"
        style={{ backgroundImage: "url('/hero-background.png')" }}
      >
        <div className="absolute inset-0 bg-gradient-to-t from-gray-900/50 to-purple-800/70" />
        <div className="relative h-full flex flex-col justify-center items-center text-center text-white p-4">
          <h1 className="text-4xl md:text-5xl font-bold mb-4 leading-tight drop-shadow-md">
            좋아하는 아티스트와
            <br />
            함께 즐기는 시간
          </h1>
          <p className="text-lg md:text-xl max-w-2xl drop-shadow-md">
            실시간으로 음악을 시청하고 팬들과 채팅으로 소통해보세요
          </p>
          <div className="mt-8 flex gap-3">
            <Link
              to="/artist-list"
              className="px-8 py-3 bg-white text-purple-700 font-semibold rounded-full shadow-lg transition-transform duration-300 hover:scale-105"
            >
              아티스트 둘러보기
            </Link>
            <button
              onClick={() => openGuide(0)}
              className="px-5 py-3 rounded-full bg-purple-600 text-white font-semibold shadow-lg hover:bg-purple-700 flex items-center gap-2"
            >
              <HelpCircle className="h-5 w-5" />
              사용 가이드
            </button>
          </div>
        </div>
      </div>

      {/* 메인 콘텐츠 영역 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 space-y-24">
        {/* 핫한 방송 영역 */}
        <section>
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-3xl font-bold">
              🔥 지금 핫한 방{" "}
              {trendingRooms && trendingRooms.roomInfoList.length > 0
                ? `Top ${trendingRooms.roomInfoList.length}`
                : null}
            </h2>
            {trendingRooms && trendingRooms.roomInfoList.length > 0 && (
              <Link
                to="/room-list"
                className="text-purple-600 hover:text-purple-800 font-semibold transition-colors"
              >
                전체 보기 →
              </Link>
            )}
          </div>
          <div className="flex flex-wrap justify-center gap-8 flex-grow">
            {isLoadingTrending ? (
              Array.from({ length: 3 }).map((_, i) => (
                <VideoCardSkeleton key={i} />
              ))
            ) : trendingError ? (
              <p className="w-full text-center text-red-500 py-20">
                {trendingError}
              </p>
            ) : trendingRooms?.roomInfoList &&
              trendingRooms?.roomInfoList.length > 0 ? (
              trendingRooms.roomInfoList.map((room) => (
                <VideoCard
                  key={room.roomId}
                  roomId={room.roomId}
                  title={room.title}
                  hostId={room.hostId}
                  hostNickname={room.hostNickname}
                  hostProfileImgUrl={room.hostProfileImgUrl ?? ""}
                  imgUrl={room.imgUrl}
                  participantCount={room.participantCount}
                />
              ))
            ) : (
              <div className="w-full flex flex-col items-center justify-center text-center text-gray-500 py-20 bg-gray-100 rounded-2xl">
                <Tv size={48} className="text-gray-300 mb-4" />
                <p className="font-semibold text-gray-600">
                  아직 생성된 방이 없습니다.
                </p>
                <p className="text-sm mt-1">가장 먼저 라이브를 시작해보세요!</p>
              </div>
            )}
          </div>
        </section>

        {/* 아티스트 목록 영역 */}
        <section>
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-3xl font-bold">주목해야 할 아티스트!</h2>
            <Link
              to="/artist-list"
              className="text-purple-600 hover:text-purple-800 font-semibold transition-colors"
            >
              더보기 →
            </Link>
          </div>
          <div className="flex flex-wrap justify-center gap-4">
            {isLoadingArtists
              ? Array.from({ length: 5 }).map((_, i) => (
                  <ArtistCardSkeleton key={i} />
                ))
              : recommendedArtists.map((artist) => (
                  <ArtistCard
                    key={artist.artistId}
                    {...artist}
                    onClick={() =>
                      handleCardClick(artist.artistId, artist.nameEn)
                    }
                  />
                ))}
          </div>
        </section>

        {/* === Guide Section === */}
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl md:text-3xl font-bold">빠르게 시작하기</h2>
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            {guideSteps.map((s, i) => (
              <button
                key={i}
                onClick={() => openGuide(i)}
                className="group relative rounded-2xl overflow-hidden bg-white shadow hover:shadow-lg transition-all text-left"
              >
                <img
                  src={s.img}
                  alt={s.alt}
                  className="w-full h-48 object-cover"
                  loading="lazy"
                />
                <div className="p-4">
                  <p className="text-sm font-semibold text-purple-600">
                    STEP {i + 1}
                  </p>
                  <h3 className="text-lg font-bold mt-1">{s.title}</h3>
                  <p className="text-gray-600 mt-1 line-clamp-2">{s.desc}</p>
                  <span className="inline-block mt-3 text-purple-600 group-hover:translate-x-0.5 transition">
                    자세히 보기 →
                  </span>
                </div>
              </button>
            ))}
          </div>
        </section>
      </main>

      {/* 가이드 모달 */}
      <GuideModal
        open={guideOpen}
        steps={guideSteps}
        index={guideIndex}
        onClose={() => setGuideOpen(false)}
        onPrev={prevGuide}
        onNext={nextGuide}
      />
    </div>
  );
};

export default HomePage;
