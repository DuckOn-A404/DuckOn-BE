import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom"; // 추가
import type { FollowUser } from "../../../types/follow";
import { fetchFollowList } from "../../../api/follow/followFollowingList";
import { followUser, unfollowUser } from "../../../api/follow/followService";

type FollowerListProps = {
  onClose: () => void;
};

const FollowerList = ({ onClose }: FollowerListProps) => {
  const [followers, setFollowers] = useState<FollowUser[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    const loadFollowers = async () => {
      try {
        const data = await fetchFollowList();
        setFollowers(data);
      } catch {}
    };
    loadFollowers();
  }, []);

  const toggleFollow = async (user: FollowUser) => {
    try {
      if (user.following) {
        await unfollowUser(user.userId);
      } else {
        await followUser(user.userId);
      }
      setFollowers((prev) =>
        prev.map((f) =>
          f.userId === user.userId ? { ...f, following: !f.following } : f
        )
      );
    } catch {
      alert("팔로우 처리 중 문제가 발생했습니다.");
    }
  };

  const goToUser = (userId: string) => {
    navigate(`/user/${userId}`); // 상세 페이지 이동
    onClose(); // 모달 닫기
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-black/50 bg-opacity-30 flex justify-center items-center"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-xl p-6 w-[350px] max-h-[80vh] overflow-y-auto relative shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-lg font-bold mb-4">팔로워</h2>
        <button
          onClick={onClose}
          className="absolute top-3 right-4 text-gray-500 hover:text-black"
          aria-label="닫기"
        >
          ✕
        </button>

        <ul className="space-y-4">
          {followers.map((user) => (
            <li
              key={user.userId}
              className="flex items-center justify-between cursor-pointer hover:bg-gray-50 rounded-lg px-2 py-1"
              onClick={() => goToUser(user.userId)} // 항목 클릭으로 이동
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") goToUser(user.userId);
              }}
            >
              <div className="flex items-center gap-3">
                <img
                  src={user.profileImg || "/default_image.png"}
                  alt="profile"
                  className="w-10 h-10 rounded-full object-cover"
                />
                <span className="text-sm font-medium text-gray-700">
                  {user.nickname}
                </span>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation(); // 이동 방지
                  toggleFollow(user);
                }}
                className={`text-sm px-3 py-1 rounded transition ${
                  user.following
                    ? "bg-gray-200 hover:bg-gray-300 text-gray-700"
                    : "bg-purple-500 text-white hover:bg-purple-600"
                }`}
              >
                {user.following ? "언팔로우" : "팔로우"}
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default FollowerList;
