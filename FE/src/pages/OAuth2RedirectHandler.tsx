import { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useUserStore } from "../store/useUserStore";
import { getMyProfileAfterOAuth } from "../api/authService";
import { emitTokenRefreshed } from "../api/axiosInstance";

/**
 * 소셜 로그인 성공 후, 백엔드로부터 리다이렉트되는 페이지입니다.
 * 사용자 정보를 가져와 상태를 업데이트하고, 홈페이지로 이동시킵니다.
 */
const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setMyUser } = useUserStore();

  useEffect(() => {
    const processLogin = async () => {
      try {
        const params = new URLSearchParams(location.search);
        const accessToken = params.get("accessToken");
        const refreshToken = params.get("refreshToken");

        if (!accessToken || !refreshToken) {
          throw new Error("토큰이 존재하지 않습니다.");
        }

        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);

        emitTokenRefreshed(accessToken)

        const userData = await getMyProfileAfterOAuth();
        setMyUser(userData);

        navigate("/");
      } catch {
        navigate("/login");
      }
    };

    processLogin();
  }, [location.search, navigate, setMyUser]);

  return (
    <div className="flex justify-center items-center h-screen">
      <p>로그인 처리 중입니다. 잠시만 기다려주세요...</p>
    </div>
  );
};

export default OAuth2RedirectHandler;
