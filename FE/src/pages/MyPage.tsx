// import { useEffect, useState } from "react";
// import { type MyUser } from "../types/mypage";
// import {
//   deleteMyAccount,
//   fetchMyProfile,
//   verifyPassword,
// } from "../api/userService";
// import PasswordConfirm from "../components/common/PasswordConfirm";
// import EditProfileCard from "../components/domain/user/EditProfileCard";
// import FollowerList from "../components/common/modal/FollowerList";
// import FollowingList from "../components/common/modal/FollowingList";
// import MyProfileCard from "../components/domain/user/MyProfileCard";
// import MyCreatedRoomsPanel from "../components/domain/room/MyCreatedRoomsPanel";
// import BlockListModal from "../components/common/modal/BlockListModal";

// const isEmptyImg = (v: unknown): boolean =>
//   v === undefined || v === null || (typeof v === "string" && v.trim() === "");
// import { useUserStore } from "../store/useUserStore";
// import { useNavigate } from "react-router-dom";
// import DeleteAccountModal from "../components/common/modal/DeleteAccountModal";

// const MyPage = () => {
//   const navigate = useNavigate();
//   const [myUser, setMyUser] = useState<MyUser | null>(null);
//   const [loading, setLoading] = useState(true);
//   const [showModal, setShowModal] = useState(false);
//   const [isEditing, setIsEditing] = useState(false);
//   const [openList, setOpenList] = useState<"follower" | "following" | null>(
//     null
//   );
//   const [isBlockModalOpen, setIsBlockModalOpen] = useState(false);

//   const syncGlobal = (u: MyUser) => {
//     useUserStore.getState().setMyUser({
//       ...(u as any),
//       // 프로젝트 타입 차이를 피하려고 artistList 기본값 보정
//       artistList: (u as any).artistList ?? [],
//     } as any);
//   };

//   const [showDeleteModal, setShowDeleteModal] = useState(false);
//   const [deleting, setDeleting] = useState(false);
//   const { setMyUser: setStoreUser } = useUserStore();

//   const handleConfirm = async (password: string): Promise<boolean> => {
//     const isValid = await verifyPassword(password);
//     if (isValid) {
//       setShowModal(false);
//       setIsEditing(true);
//       return true;
//     }
//     return false;
//   };

//   const handleOpenDeleteModal = () => {
//     setShowDeleteModal(true);
//   };

//   const handleConfirmDelete = async () => {
//     if (deleting) return;
//     try {
//       setDeleting(true);
//       await deleteMyAccount();
//       // 로컬 정리
//       localStorage.removeItem("accessToken");
//       localStorage.removeItem("refreshToken");
//       localStorage.removeItem("user-storage");
//       setMyUser(null);
//       setStoreUser(null);
//       // 모달 닫고 이동
//       setShowDeleteModal(false);
//       navigate("/");
//     } catch (e) {
//       console.error("회원 탈퇴 실패:", e);
//     } finally {
//       setDeleting(false);
//     }
//   };

//   useEffect(() => {
//     const loadUser = async () => {
//       setLoading(true);
//       try {
//         const data = await fetchMyProfile();
//         setMyUser(data);
//         syncGlobal(data);
//       } finally {
//         setLoading(false);
//       }
//     };

//     loadUser();
//   }, []);

//   useEffect(() => {
//     if (openList === null) {
//       const reloadProfile = async () => {
//         try {
//           const updated = await fetchMyProfile();
//           setMyUser((prev) => {
//             if (!prev) {
//               syncGlobal(updated);
//               return updated;
//             }
//             // imgUrl 병합: undefined/""면 이전 유지, 문자열이면 교체
//             const nextImgUrl = !isEmptyImg((updated as any).imgUrl)
//               ? (updated as any).imgUrl
//               : prev.imgUrl;

//             const next: MyUser = { ...prev, ...updated, imgUrl: nextImgUrl };
//             syncGlobal(next); // 헤더 갱신
//             return next;
//           });
//         } catch {}
//       };
//       reloadProfile();
//     }
//   }, [openList]);

//   if (loading) return <div className="p-6">불러오는 중...</div>;
//   if (!myUser) return <div className="p-6 text-red-500">사용자 정보 없음</div>;

//   const isSocial = !!myUser.socialLogin; // true면 소셜, false/undefined면 일반 로그인

//   return (
//     <div className="px-8 py-6">
//       {!isEditing ? (
//         <MyProfileCard
//           key={`mypage-${myUser.userId}`}
//           user={myUser}
//           onEditClick={() => {
//             if (isSocial) {
//               // 소셜 로그인: 모달 없이 바로 편집
//               setIsEditing(true);
//             } else {
//               // 일반 로그인: 비밀번호 확인 모달
//               setShowModal(true);
//             }
//           }}
//           onFollowerClick={() => setOpenList("follower")}
//           onFollowingClick={() => setOpenList("following")}
//           onDeleteClick={handleOpenDeleteModal}
//           onBlockListClick={() => setIsBlockModalOpen(true)}
//         />
//       ) : (
//         <EditProfileCard
//           user={myUser}
//           onCancel={() => setIsEditing(false)}
//           onUpdate={(updatedUser) => {
//             // onUpdate 병합: undefined/""는 이전 유지, 문자열은 교체
//             setMyUser((prev) => {
//               if (!prev) {
//                 syncGlobal(updatedUser as any);
//                 return updatedUser;
//               }
//               const nextImgUrl =
//                 (updatedUser as any).imgUrl !== undefined &&
//                 !isEmptyImg((updatedUser as any).imgUrl)
//                   ? (updatedUser as any).imgUrl // 문자열(새 URL 또는 DEFAULT_IMG)
//                   : prev.imgUrl; // 유지

//               const next: MyUser = {
//                 ...prev,
//                 ...updatedUser,
//                 imgUrl: nextImgUrl,
//               };
//               syncGlobal(next); // 헤더 즉시 반영
//               return next;
//             });
//             setIsEditing(false);
//           }}
//         />
//       )}
//       {/* 내가 만든 과거 방(입장 불가, 보기 전용) */}
//       <div className="mt-8">
//         <MyCreatedRoomsPanel rooms={myUser.roomList ?? []} pageSize={12} />
//       </div>

//       {/* 일반 로그인일 때만 모달 렌더링 */}
//       {!isSocial && (
//         <PasswordConfirm
//           isOpen={showModal}
//           onClose={() => setShowModal(false)}
//           onConfirm={handleConfirm}
//         />
//       )}
//       {/* 팔로워/팔로잉 모달 */}
//       {openList === "follower" && (
//         <FollowerList onClose={() => setOpenList(null)} />
//       )}
//       {openList === "following" && (
//         <FollowingList onClose={() => setOpenList(null)} />
//       )}
//       {/* 탈퇴 확인 모달 */}
//       <DeleteAccountModal
//         isOpen={showDeleteModal}
//         loading={deleting}
//         onCancel={() => setShowDeleteModal(false)}
//         onConfirm={handleConfirmDelete}
//       />

//       {/* 상태에 따라 BlockListModal 렌더링 */}
//       {isBlockModalOpen && (
//         <BlockListModal onClose={() => setIsBlockModalOpen(false)} />
//       )}
//     </div>
//   );
// };

// export default MyPage;


// import { useEffect, useState } from "react";
// import { type MyUser } from "../types/mypage";
// import {
//   deleteMyAccount,
//   fetchMyProfile,
//   verifyPassword,
// } from "../api/userService";
// import PasswordConfirm from "../components/common/PasswordConfirm";
// import EditProfileCard from "../components/domain/user/EditProfileCard";
// import FollowerList from "../components/common/modal/FollowerList";
// import FollowingList from "../components/common/modal/FollowingList";
// import MyProfileCard from "../components/domain/user/MyProfileCard";
// import MyCreatedRoomsPanel from "../components/domain/room/MyCreatedRoomsPanel";
// import BlockListModal from "../components/common/modal/BlockListModal";

// const isEmptyImg = (v: unknown): boolean =>
//   v === undefined || v === null || (typeof v === "string" && v.trim() === "");
// import { useUserStore } from "../store/useUserStore";
// import { useNavigate } from "react-router-dom";
// import DeleteAccountModal from "../components/common/modal/DeleteAccountModal";

// const MyPage = () => {
//   const navigate = useNavigate();
//   const [myUser, setMyUser] = useState<MyUser | null>(null);
//   const [loading, setLoading] = useState(true);
//   const [showModal, setShowModal] = useState(false);
//   const [isEditing, setIsEditing] = useState(false);
//   const [openList, setOpenList] = useState<"follower" | "following" | null>(
//     null
//   );
//   const [isBlockModalOpen, setIsBlockModalOpen] = useState(false);

//   const syncGlobal = (u: MyUser) => {
//     useUserStore.getState().setMyUser({
//       ...(u as any),
//       // 프로젝트 타입 차이를 피하려고 artistList 기본값 보정
//       artistList: (u as any).artistList ?? [],
//     } as any);
//   };

//   const [showDeleteModal, setShowDeleteModal] = useState(false);
//   const [deleting, setDeleting] = useState(false);
//   const { setMyUser: setStoreUser } = useUserStore();

//   const handleConfirm = async (password: string): Promise<boolean> => {
//     const isValid = await verifyPassword(password);
//     if (isValid) {
//       setShowModal(false);
//       setIsEditing(true);
//       return true;
//     }
//     return false;
//   };

//   const handleOpenDeleteModal = () => {
//     setShowDeleteModal(true);
//   };

//   const handleConfirmDelete = async () => {
//     if (deleting) return;
//     try {
//       setDeleting(true);
//       await deleteMyAccount();
//       // 로컬 정리
//       localStorage.removeItem("accessToken");
//       localStorage.removeItem("refreshToken");
//       localStorage.removeItem("user-storage");
//       setMyUser(null);
//       setStoreUser(null);
//       // 모달 닫고 이동
//       setShowDeleteModal(false);
//       navigate("/");
//     } catch (e) {
//       console.error("회원 탈퇴 실패:", e);
//     } finally {
//       setDeleting(false);
//     }
//   };

//   useEffect(() => {
//     const loadUser = async () => {
//       setLoading(true);
//       try {
//         const data = await fetchMyProfile();

//         // ✅ 최초 로드에서도 imgUrl 병합 처리
//         // 서버가 빈 값(null / "")을 주면, 이전(스토어/현 상태)의 이미지를 유지
//         setMyUser((prev) => {
//           const storePrev = useUserStore.getState().myUser as any;
//           const prevImg = (prev as any)?.imgUrl ?? storePrev?.imgUrl;
//           const incomingImg = (data as any)?.imgUrl;
//           const nextImgUrl = !isEmptyImg(incomingImg) ? incomingImg : prevImg;

//           const next: MyUser = { ...(prev ?? {}), ...data, imgUrl: nextImgUrl } as MyUser;
//           syncGlobal(next);
//           return next;
//         });
//       } finally {
//         setLoading(false);
//       }
//     };

//     loadUser();
//   }, []);

//   useEffect(() => {
//     if (openList === null) {
//       const reloadProfile = async () => {
//         try {
//           const updated = await fetchMyProfile();
//           setMyUser((prev) => {
//             if (!prev) {
//               syncGlobal(updated);
//               return updated;
//             }
//             // imgUrl 병합: undefined/""면 이전 유지, 문자열이면 교체
//             const nextImgUrl = !isEmptyImg((updated as any).imgUrl)
//               ? (updated as any).imgUrl
//               : prev.imgUrl;

//             const next: MyUser = { ...prev, ...updated, imgUrl: nextImgUrl };
//             syncGlobal(next); // 헤더 갱신
//             return next;
//           });
//         } catch {}
//       };
//       reloadProfile();
//     }
//   }, [openList]);

//   if (loading) return <div className="p-6">불러오는 중...</div>;
//   if (!myUser) return <div className="p-6 text-red-500">사용자 정보 없음</div>;

//   const isSocial = !!myUser.socialLogin; // true면 소셜, false/undefined면 일반 로그인

//   return (
//     <div className="px-8 py-6">
//       {!isEditing ? (
//         <MyProfileCard
//           key={`mypage-${myUser.userId}`}
//           user={myUser}
//           onEditClick={() => {
//             if (isSocial) {
//               // 소셜 로그인: 모달 없이 바로 편집
//               setIsEditing(true);
//             } else {
//               // 일반 로그인: 비밀번호 확인 모달
//               setShowModal(true);
//             }
//           }}
//           onFollowerClick={() => setOpenList("follower")}
//           onFollowingClick={() => setOpenList("following")}
//           onDeleteClick={handleOpenDeleteModal}
//           onBlockListClick={() => setIsBlockModalOpen(true)}
//         />
//       ) : (
//         <EditProfileCard
//           user={myUser}
//           onCancel={() => setIsEditing(false)}
//           onUpdate={(updatedUser) => {
//             // onUpdate 병합: undefined/""는 이전 유지, 문자열은 교체
//             setMyUser((prev) => {
//               if (!prev) {
//                 syncGlobal(updatedUser as any);
//                 return updatedUser;
//               }
//               const nextImgUrl =
//                 (updatedUser as any).imgUrl !== undefined &&
//                 !isEmptyImg((updatedUser as any).imgUrl)
//                   ? (updatedUser as any).imgUrl // 문자열(새 URL 또는 DEFAULT_IMG)
//                   : prev.imgUrl; // 유지

//               const next: MyUser = {
//                 ...prev,
//                 ...updatedUser,
//                 imgUrl: nextImgUrl,
//               };
//               syncGlobal(next); // 헤더 즉시 반영
//               return next;
//             });
//             setIsEditing(false);
//           }}
//         />
//       )}
//       {/* 내가 만든 과거 방(입장 불가, 보기 전용) */}
//       <div className="mt-8">
//         <MyCreatedRoomsPanel rooms={myUser.roomList ?? []} pageSize={12} />
//       </div>

//       {/* 일반 로그인일 때만 모달 렌더링 */}
//       {!isSocial && (
//         <PasswordConfirm
//           isOpen={showModal}
//           onClose={() => setShowModal(false)}
//           onConfirm={handleConfirm}
//         />
//       )}
//       {/* 팔로워/팔로잉 모달 */}
//       {openList === "follower" && (
//         <FollowerList onClose={() => setOpenList(null)} />
//       )}
//       {openList === "following" && (
//         <FollowingList onClose={() => setOpenList(null)} />
//       )}
//       {/* 탈퇴 확인 모달 */}
//       <DeleteAccountModal
//         isOpen={showDeleteModal}
//         loading={deleting}
//         onCancel={() => setShowDeleteModal(false)}
//         onConfirm={handleConfirmDelete}
//       />

//       {/* 상태에 따라 BlockListModal 렌더링 */}
//       {isBlockModalOpen && (
//         <BlockListModal onClose={() => setIsBlockModalOpen(false)} />
//       )}
//     </div>
//   );
// };

// export default MyPage;


import { useEffect, useState } from "react";
import { type MyUser } from "../types/mypage";
import {
  deleteMyAccount,
  fetchMyProfile,
  verifyPassword,
} from "../api/userService";
import PasswordConfirm from "../components/common/PasswordConfirm";
import EditProfileCard from "../components/domain/user/EditProfileCard";
import FollowerList from "../components/common/modal/FollowerList";
import FollowingList from "../components/common/modal/FollowingList";
import MyProfileCard from "../components/domain/user/MyProfileCard";
import MyCreatedRoomsPanel from "../components/domain/room/MyCreatedRoomsPanel";
import BlockListModal from "../components/common/modal/BlockListModal";

const isEmptyImg = (v: unknown): boolean =>
  v === undefined || v === null || (typeof v === "string" && v.trim() === "");
import { useUserStore } from "../store/useUserStore";
import { useNavigate } from "react-router-dom";
import DeleteAccountModal from "../components/common/modal/DeleteAccountModal";

// --- 추가: userId별 마지막 imgUrl을 보관/복구 ---
const lastImgKey = (userId: string) => `last-img:${userId}`;
const saveLastImg = (userId?: string, url?: string) => {
  if (!userId) return;
  if (url && url.trim() !== "") {
    localStorage.setItem(lastImgKey(userId), url);
  }
};
const loadLastImg = (userId?: string): string | undefined => {
  if (!userId) return undefined;
  const v = localStorage.getItem(lastImgKey(userId)) || undefined;
  return v && v.trim() !== "" ? v : undefined;
};

const MyPage = () => {
  const navigate = useNavigate();
  const [myUser, setMyUser] = useState<MyUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [openList, setOpenList] = useState<"follower" | "following" | null>(
    null
  );
  const [isBlockModalOpen, setIsBlockModalOpen] = useState(false);

  const syncGlobal = (u: MyUser) => {
    useUserStore.getState().setMyUser({
      ...(u as any),
      artistList: (u as any).artistList ?? [],
    } as any);
  };

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const { setMyUser: setStoreUser } = useUserStore();

  const handleConfirm = async (password: string): Promise<boolean> => {
    const isValid = await verifyPassword(password);
    if (isValid) {
      setShowModal(false);
      setIsEditing(true);
      return true;
    }
    return false;
  };

  const handleOpenDeleteModal = () => {
    setShowDeleteModal(true);
  };

  const handleConfirmDelete = async () => {
    if (deleting) return;
    try {
      setDeleting(true);
      await deleteMyAccount();
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user-storage");
      setMyUser(null);
      setStoreUser(null);
      setShowDeleteModal(false);
      navigate("/");
    } catch (e) {
      console.error("회원 탈퇴 실패:", e);
    } finally {
      setDeleting(false);
    }
  };

  // ✅ 최초 로드: 서버 imgUrl이 비면 userId별 보관 값으로 복구
  useEffect(() => {
    const loadUser = async () => {
      setLoading(true);
      try {
        const data = await fetchMyProfile();
        setMyUser((prev) => {
          const uid = (data as any)?.userId ?? (prev as any)?.userId;
          const incomingImg = (data as any)?.imgUrl;
          const storedImg = loadLastImg(uid);
          const chosenImg = !isEmptyImg(incomingImg) ? incomingImg : storedImg;

          const next: MyUser = { ...(prev ?? {}), ...data, imgUrl: chosenImg } as MyUser;
          // 마지막으로 확인된 유효 이미지 저장(다음 로그인 대비)
          saveLastImg(uid, next.imgUrl as any);
          syncGlobal(next);
          return next;
        });
      } finally {
        setLoading(false);
      }
    };
    loadUser();
  }, []);

  // ✅ 이후 재조회도 동일한 병합 + 보관
  useEffect(() => {
    if (openList === null) {
      const reloadProfile = async () => {
        try {
          const updated = await fetchMyProfile();
          setMyUser((prev) => {
            if (!prev) {
              syncGlobal(updated);
              // userId가 있으면 저장
              saveLastImg((updated as any)?.userId, (updated as any)?.imgUrl);
              return updated;
            }
            const nextImgUrl = !isEmptyImg((updated as any).imgUrl)
              ? (updated as any).imgUrl
              : prev.imgUrl;

            const next: MyUser = { ...prev, ...updated, imgUrl: nextImgUrl };
            // 유효 이미지면 보관
            saveLastImg((next as any)?.userId, nextImgUrl as any);
            syncGlobal(next);
            return next;
          });
        } catch {}
      };
      reloadProfile();
    }
  }, [openList]);

  if (loading) return <div className="p-6">불러오는 중...</div>;
  if (!myUser) return <div className="p-6 text-red-500">사용자 정보 없음</div>;

  const isSocial = !!myUser.socialLogin;

  return (
    <div className="px-8 py-6">
      {!isEditing ? (
        <MyProfileCard
          key={`mypage-${myUser.userId}`}
          user={myUser}
          onEditClick={() => {
            if (isSocial) {
              setIsEditing(true);
            } else {
              setShowModal(true);
            }
          }}
          onFollowerClick={() => setOpenList("follower")}
          onFollowingClick={() => setOpenList("following")}
          onDeleteClick={handleOpenDeleteModal}
          onBlockListClick={() => setIsBlockModalOpen(true)}
        />
      ) : (
        <EditProfileCard
          user={myUser}
          onCancel={() => setIsEditing(false)}
          onUpdate={(updatedUser) => {
            setMyUser((prev) => {
              if (!prev) {
                syncGlobal(updatedUser as any);
                // 업데이트된 이미지가 유효하면 보관
                saveLastImg((updatedUser as any)?.userId, (updatedUser as any)?.imgUrl);
                return updatedUser;
              }
              const nextImgUrl =
                (updatedUser as any).imgUrl !== undefined &&
                !isEmptyImg((updatedUser as any).imgUrl)
                  ? (updatedUser as any).imgUrl
                  : prev.imgUrl;

              const next: MyUser = { ...prev, ...updatedUser, imgUrl: nextImgUrl };
              // 유효 이미지면 보관
              saveLastImg((next as any)?.userId, nextImgUrl as any);
              syncGlobal(next);
              return next;
            });
            setIsEditing(false);
          }}
        />
      )}

      <div className="mt-8">
        <MyCreatedRoomsPanel rooms={myUser.roomList ?? []} pageSize={12} />
      </div>

      {!isSocial && (
        <PasswordConfirm
          isOpen={showModal}
          onClose={() => setShowModal(false)}
          onConfirm={handleConfirm}
        />
      )}

      {openList === "follower" && <FollowerList onClose={() => setOpenList(null)} />}
      {openList === "following" && <FollowingList onClose={() => setOpenList(null)} />}

      <DeleteAccountModal
        isOpen={showDeleteModal}
        loading={deleting}
        onCancel={() => setShowDeleteModal(false)}
        onConfirm={handleConfirmDelete}
      />

      {isBlockModalOpen && <BlockListModal onClose={() => setIsBlockModalOpen(false)} />}
    </div>
  );
};

export default MyPage;
