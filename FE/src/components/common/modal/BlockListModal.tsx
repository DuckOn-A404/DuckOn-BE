import { useEffect, useState } from "react";
import ConfirmModal from "./ConfirmModal";
import { type BlockedUser } from "../../../api/userService";
import { getBlockedUsers, unblockUser } from "../../../api/userService";

type BlockListModalProps = {
  onClose: () => void;
};

const BlockListModal = ({ onClose }: BlockListModalProps) => {
  const [blockedUsers, setBlockedUsers] = useState<BlockedUser[]>([]);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<BlockedUser | null>(null);

  useEffect(() => {
    const loadBlockedUsers = async () => {
      try {
        const data = await getBlockedUsers();
        setBlockedUsers(data);
      } catch (error) {
        console.error("차단 목록을 불러오는 데 실패했습니다.", error);
        alert("차단 목록을 불러오는 중 오류가 발생했습니다.");
      }
    };
    loadBlockedUsers();
  }, []);

  // "차단 해제" 버튼 클릭 시
  const handleUnblockClick = (user: BlockedUser) => {
    setSelectedUser(user); // 어떤 유저인지 저장
    setIsConfirmOpen(true); // 모달 열기
  };

  // 모달에서 "취소"를 눌렀을 때 실행될 함수
  const handleCancelUnblock = () => {
    setIsConfirmOpen(false); // 모달 닫기
    setSelectedUser(null); // 선택된 유저 정보 초기화
  };

  // 모달에서 "확인"을 눌렀을 때 실행될 함수
  const handleConfirmUnblock = async () => {
    if (!selectedUser) return;

    try {
      await unblockUser(selectedUser.userId);
      setBlockedUsers((prev) =>
        prev.filter((user) => user.userId !== selectedUser.userId)
      );
    } catch {
      alert("차단 해제 중 문제가 발생했습니다.");
    } finally {
      setIsConfirmOpen(false);
      setSelectedUser(null);
    }
  };

  return (
    <>
      <div
        className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center"
        onClick={onClose}
      >
        <div
          className="bg-white rounded-xl p-6 w-[350px] max-h-[80vh] overflow-y-auto relative shadow-xl"
          onClick={(e) => e.stopPropagation()}
        >
          <h2 className="text-lg font-bold mb-4">차단 목록 관리</h2>
          <button
            onClick={onClose}
            className="absolute top-3 right-4 text-gray-500 hover:text-black"
            aria-label="닫기"
          >
            ✕
          </button>

          <ul className="space-y-4">
            {blockedUsers.length > 0 ? (
              blockedUsers.map((user) => (
                <li
                  key={user.userId}
                  className="flex items-center justify-between"
                >
                  <div className="flex items-center gap-3">
                    <img
                      src={user.imgUrl || "/default_image.png"}
                      alt="profile"
                      className="w-10 h-10 rounded-full object-cover"
                    />
                    <span className="text-sm font-medium text-gray-700">
                      {user.nickname}
                    </span>
                  </div>
                  <button
                    // 4. onClick 핸들러를 새로 만든 함수로 교체합니다.
                    onClick={() => handleUnblockClick(user)}
                    className="text-sm px-3 py-1 rounded transition bg-gray-200 hover:bg-gray-300 text-gray-700"
                  >
                    차단 해제
                  </button>
                </li>
              ))
            ) : (
              <p className="text-center text-sm text-gray-500 py-4">
                차단한 사용자가 없습니다.
              </p>
            )}
          </ul>
        </div>
      </div>

      {/* 5. ConfirmModal 컴포넌트를 렌더링하고 상태와 핸들러를 연결합니다. */}
      <ConfirmModal
        isOpen={isConfirmOpen}
        title="사용자 차단 해제"
        description={`정말로 '${selectedUser?.nickname}'님을 차단 해제하시겠습니까?`}
        confirmText="해제"
        onConfirm={handleConfirmUnblock}
        onCancel={handleCancelUnblock}
      />
    </>
  );
};
export default BlockListModal;
