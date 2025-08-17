import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { type SignupData } from "../types/auth";
import {
  postSignup,
  checkEmailExists,
  checkUserIdExists,
} from "../api/authService";

type SignupFormData = SignupData & {
  passwordConfirm: string;
};

const isValidEmail = (email: string): boolean => {
  const regex = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/;
  return regex.test(email);
};

export const useSignupForm = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<SignupFormData>({
    email: "",
    userId: "",
    password: "",
    passwordConfirm: "",
    nickname: "",
    language: "ko",
    profileImg: null,
  });

  const [emailError, setEmailError] = useState("");
  const [userIdError, setUserIdError] = useState("");

  const [emailSuccess, setEmailSuccess] = useState("");
  const [userIdSuccess, setUserIdSuccess] = useState("");

  const [emailChecked, setEmailChecked] = useState(false);
  const [userIdChecked, setUserIdChecked] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [passwordConfirmError, setPasswordConfirmError] = useState("");
  const [passwordError, setPasswordError] = useState("");

  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,}$/;

  const validatePassword = (pwd: string): string | undefined => {
    if (!pwd) return "비밀번호를 입력해주세요.";
    if (!passwordRegex.test(pwd)) {
      return "영문, 숫자, 특수문자를 각각 1자 이상 포함하고 최소 8자여야 합니다.";
    }
    return undefined;
  };

  const validatePasswordConfirm = (
    pwd: string,
    confirm: string
  ): string | undefined => {
    if (!confirm) return "비밀번호 확인을 입력해주세요.";
    if (pwd !== confirm) return "비밀번호가 일치하지 않습니다.";
    return undefined;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    if (error) setError(null);

    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    if (name === "email") {
      setEmailError("");
      setEmailSuccess("");
      setEmailChecked(false);
      if (value && !isValidEmail(value)) {
        setEmailError("유효한 이메일 형식이 아닙니다.");
      }
    } else if (name === "userId") {
      setUserIdError("");
      setUserIdSuccess("");
      setUserIdChecked(false);
    } else if (name === "password") {
      const err = validatePassword(value);
      setPasswordError(err ?? "");
      if (formData.passwordConfirm) {
        const cErr = validatePasswordConfirm(value, formData.passwordConfirm);
        setPasswordConfirmError(cErr ?? "");
      }
    } else if (name === "passwordConfirm") {
      const cErr = validatePasswordConfirm(formData.password, value);
      setPasswordConfirmError(cErr ?? "");
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setFormData((prev) => ({ ...prev, profileImg: file }));
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!emailChecked) {
      setError("이메일 중복 확인을 완료해주세요.");
      return;
    }
    if (!userIdChecked) {
      setError("아이디 중복 확인을 완료해주세요.");
      return;
    }
    if (
      !formData.email ||
      !formData.userId ||
      !formData.password ||
      !formData.passwordConfirm
    ) {
      setError("필수 항목을 모두 입력해주세요.");
      return;
    }

    // 제출 직전 최종 검증
    const pwdErr = validatePassword(formData.password);
    const confirmErr = validatePasswordConfirm(
      formData.password,
      formData.passwordConfirm
    );
    setPasswordError(pwdErr ?? "");
    setPasswordConfirmError(confirmErr ?? "");
    if (pwdErr || confirmErr) {
      setError(pwdErr || confirmErr || "비밀번호를 확인해주세요.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const { passwordConfirm, ...rest } = formData;

      if (!rest.nickname.trim()) {
        rest.nickname = "익명의 사용자";
      }

      const form = new FormData();
      Object.entries(rest).forEach(([key, value]) => {
        if (value === null || value === undefined) return;
        if (value instanceof File) form.append(key, value);
        else form.append(key, String(value));
      });

      await postSignup(form);
      navigate("/login");
    } catch (err: any) {
      setError(err.response?.data?.message || "오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleCheckEmail = async () => {
    setEmailError("");
    setEmailSuccess("");

    if (!formData.email) {
      setEmailError("이메일을 입력해주세요.");
      return;
    }
    if (!isValidEmail(formData.email)) {
      setEmailError("유효한 이메일 형식이 아닙니다.");
      return;
    }

    try {
      const res = await checkEmailExists(formData.email);
      setEmailChecked(true);
      if (res.isDuplicate) setEmailError("이미 사용 중인 이메일입니다.");
      else setEmailSuccess("사용 가능한 이메일입니다.");
    } catch {
      setEmailError("중복 확인 중 오류가 발생했습니다.");
    }
  };

  const handleCheckUserId = async () => {
    setUserIdError("");
    setUserIdSuccess("");

    if (!formData.userId) {
      setUserIdError("아이디를 입력해주세요.");
      return;
    }

    try {
      const res = await checkUserIdExists(formData.userId);
      setUserIdChecked(true);
      if (res.isDuplicate) setUserIdError("이미 사용 중인 아이디입니다.");
      else setUserIdSuccess("사용 가능한 아이디입니다.");
    } catch {
      setUserIdError("중복 확인 중 오류가 발생했습니다.");
    }
  };

  return {
    formData,
    loading,
    error,
    handleChange,
    handleFileChange,
    handleSubmit,
    emailError,
    userIdError,
    emailSuccess,
    userIdSuccess,
    handleCheckEmail,
    handleCheckUserId,
    emailChecked,
    userIdChecked,
    passwordConfirmError,
    passwordError,
  };
};
