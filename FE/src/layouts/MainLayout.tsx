import Header from "../components/common/Header";
import Footer from "../components/common/Footer";
import {Outlet, useNavigate} from "react-router-dom";
import {useUserStore} from "../store/useUserStore";
import {logoutUser} from "../api/authService";
import { emitTokenRefreshed } from "../api/axiosInstance";

const MainLayout = () => {
  const navigate = useNavigate();
  const {myUser, setMyUser} = useUserStore();

  const handleLogin = () => navigate("/login");

  const handleLogout = async () => {
    try {
      await logoutUser(); // 서버에 리프레시 블랙리스트 등록
    } catch (e) {
      console.warn("서버 로그아웃 실패(무시하고 로컬 정리 진행):", e);
    } finally {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user-storage");
      emitTokenRefreshed(null)
      
      setMyUser(null);
      navigate("/");
    }
  };

  const handleSignup = () => {
    navigate("/signup");
  };

  return (
    <div>
      {/* 헤더 */}
      <Header
        user={myUser}
        onLogin={handleLogin}
        onLogout={handleLogout}
        onSignup={handleSignup}
      />

      {/* 본문 */}
      <main>
        <Outlet />
      </main>

      {/* 푸터 */}
      <div className="mt-8">
        <Footer />
      </div>
    </div>
  );
};

export default MainLayout;
