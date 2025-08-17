import type { OtherUser } from "../../../types/otherUser";

type OtherProfileCardProps = {
  user: OtherUser;
  onToggleFollow: () => void;
  // 타인 페이지에서는 아래 두 핸들러를 쓰지 않습니다(비인터랙티브).
  onFollowerClick?: () => void;
  onFollowingClick?: () => void;
  isFollowLoading?: boolean;
};

const OtherProfileCard = ({
  user,
  onToggleFollow,
}: OtherProfileCardProps) => {
  return (
    <div className="bg-white rounded-xl px-4 sm:px-8 py-6 mb-10 w-full max-w-[880px] mx-auto shadow-sm">
      {/* 헤더 */}
      <div className="flex justify-between items-center mb-6 gap-3">
        <h1 className="text-lg font-bold text-gray-800">프로필 정보</h1>
        <button
          onClick={onToggleFollow}
          className={`w-20 flex-shrink-0 px-2 py-1.5 rounded-full text-sm font-semibold transition-colors ${
            user.following
              ? "bg-gray-200 text-gray-500 hover:bg-gray-300"
              : "bg-purple-500 text-white hover:bg-purple-600"
          }`}
        >
          {user.following ? "팔로잉" : "팔로우"}
        </button>
      </div>

      {/* 본문 */}
      <div className="flex flex-col sm:flex-row gap-6 sm:gap-8 items-start">
        {/* 왼쪽: 프로필 + 팔/팔 (비인터랙티브) */}
        <div className="flex flex-col items-center w-full sm:w-32 shrink-0">
          <img
            src={user.imgUrl || "/default_image.png"}
            alt="프로필 이미지"
            className="w-24 h-24 object-cover rounded-full"
          />

          <div className="mt-4 flex gap-10 sm:gap-6 text-center select-none">
            <div className="text-center">
              <div className="text-lg font-bold">{user.followerCount?.toLocaleString() ?? "0"}</div>
              <div className="text-xs text-gray-500">팔로워</div>
            </div>
            <div className="text-center">
              <div className="text-lg font-bold">{user.followingCount?.toLocaleString() ?? "0"}</div>
              <div className="text-xs text-gray-500">팔로잉</div>
            </div>
          </div>
        </div>

        {/* 오른쪽: 라벨 옆 값(항상 가로 배치) */}
        <div className="min-w-0 flex-1 text-sm space-y-2">
          <div className="flex items-start gap-2">
            <div className="shrink-0 w-20 sm:w-24 text-gray-500 font-medium">아이디</div>
            <div className="min-w-0 flex-1 break-all sm:whitespace-nowrap sm:truncate">
              {user.userId}
            </div>
          </div>
          <div className="flex items-start gap-2">
            <div className="shrink-0 w-20 sm:w-24 text-gray-500 font-medium">닉네임</div>
            <div className="min-w-0 flex-1 break-all sm:whitespace-nowrap sm:truncate font-semibold">
              {user.nickname}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OtherProfileCard;
