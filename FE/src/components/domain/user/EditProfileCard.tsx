// import { useState, useRef } from "react";
// import type { MyUser } from "../../../types/mypage";
// import { fetchMyProfile, updateUserProfile } from "../../../api/userService";
// import { Camera } from "lucide-react";
// import { useUserStore } from "../../../store/useUserStore";

// export type EditProfileCardProps = {
//   user: MyUser;
//   onCancel: () => void;
//   onUpdate: (updatedUser: MyUser) => void;
// };

// const DEFAULT_IMG = "/default_image.png";

// // 영문/숫자/특수문자 각 1개 이상 + 공백 불가 + 8자 이상
// const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,}$/;

// const EditProfileCard = ({
//   user,
//   onCancel,
//   onUpdate,
// }: EditProfileCardProps) => {
//   const [nickname, setNickname] = useState(user.nickname);
//   const [profileImage, setProfileImage] = useState<File | null>(null);
//   const [previewUrl, setPreviewUrl] = useState<string>(
//     user.imgUrl ?? DEFAULT_IMG
//   );
//   const [showImageOptions, setShowImageOptions] = useState(false);
//   const [didPickNewImage, setDidPickNewImage] = useState(false);

//   // 소셜 로그인 여부 (true면 소셜)
//   const isSocial = !!(user as any).socialLogin;

//   // 비밀번호 관련 상태 (일반 로그인 사용자에게만 의미 있음)
//   const [newPassword, setNewPassword] = useState("");
//   const [confirmPassword, setConfirmPassword] = useState("");
//   const [newPasswordError, setNewPasswordError] = useState("");
//   const [confirmPasswordError, setConfirmPasswordError] = useState("");

//   const fileInputRef = useRef<HTMLInputElement | null>(null);

//   const handleImageClick = () => {
//     fileInputRef.current?.click();
//   };

//   const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
//     const file = e.target.files?.[0];
//     if (file) {
//       setProfileImage(file);
//       setPreviewUrl(URL.createObjectURL(file));
//       setShowImageOptions(false);
//       setDidPickNewImage(true);
//     }
//   };

//   const handleResetToDefaultImage = () => {
//     setPreviewUrl(DEFAULT_IMG);
//     setProfileImage(null);
//     setShowImageOptions(false);
//     setDidPickNewImage(false);
//   };

//   const handleCameraClick = () => {
//     if (previewUrl === DEFAULT_IMG || user.imgUrl === null) {
//       fileInputRef.current?.click();
//     } else {
//       setShowImageOptions(!showImageOptions);
//     }
//   };

//   const handleSubmit = async () => {
//     // 소셜 로그인 계정은 비밀번호 검증 자체를 생략
//     if (!isSocial) {
//       if (newPassword) {
//         if (!PASSWORD_REGEX.test(newPassword)) {
//           setNewPasswordError(
//             "영문, 숫자, 특수문자를 각각 1자 이상 포함하고 최소 8자여야 합니다."
//           );
//           return;
//         } else {
//           setNewPasswordError("");
//         }
//         if (newPassword !== confirmPassword) {
//           setConfirmPasswordError("새 비밀번호와 확인이 일치하지 않습니다.");
//           return;
//         } else {
//           setConfirmPasswordError("");
//         }
//       }
//     }

//     const isReset =
//       !profileImage && !!user.imgUrl && previewUrl === DEFAULT_IMG;

//     const formData = new FormData();
//     formData.append("nickname", nickname);
//     formData.append("language", "ko");

//     // 소셜 로그인일 경우엔 newPassword를 절대 보내지 않음
//     if (!isSocial && newPassword) {
//       formData.append("newPassword", newPassword);
//     }
//     if (profileImage) {
//       formData.append("profileImg", profileImage);
//     }

//     try {
//       await updateUserProfile(formData);
//       const refreshed = didPickNewImage ? await fetchMyProfile() : null;

//       const next: MyUser = {
//         ...(refreshed ?? user),
//         nickname,
//         imgUrl: didPickNewImage
//           ? refreshed?.imgUrl ?? user.imgUrl
//           : isReset
//           ? DEFAULT_IMG
//           : user.imgUrl ?? undefined,
//       };

//       useUserStore.getState().setMyUser({
//         ...next,
//         artistList: next.artistList ?? [],
//       });

//       onUpdate(next);
//     } catch (err) {
//       alert("프로필 수정 중 오류가 발생했습니다.");
//     }
//   };

//   const handleNewPasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
//     const val = e.target.value;
//     setNewPassword(val);

//     if (val && !PASSWORD_REGEX.test(val)) {
//       setNewPasswordError(
//         "영문, 숫자, 특수문자를 각각 1자 이상 포함하고 최소 8자여야 합니다."
//       );
//     } else {
//       setNewPasswordError("");
//     }

//     if (confirmPassword && val !== confirmPassword) {
//       setConfirmPasswordError("비밀번호가 일치하지 않습니다.");
//     } else {
//       setConfirmPasswordError("");
//     }
//   };

//   const handleConfirmPasswordChange = (
//     e: React.ChangeEvent<HTMLInputElement>
//   ) => {
//     const val = e.target.value;
//     setConfirmPassword(val);

//     if (newPassword && newPassword !== val) {
//       setConfirmPasswordError("비밀번호가 일치하지 않습니다.");
//     } else {
//       setConfirmPasswordError("");
//     }
//   };

//   return (
//     <div className="bg-white rounded-xl px-4 sm:px-8 py-6 mb-10 w-full max-w-[880px] mx-auto shadow-sm">
//       {/* 헤더 */}
//       <div className="flex justify-between items-center mb-6">
//         <h1 className="text-lg font-bold">프로필 수정</h1>
//         <div className="space-x-2">
//           <button
//             className="text-sm text-purple-600 font-medium hover:underline transition"
//             onClick={handleSubmit}
//           >
//             저장
//           </button>
//           <button
//             className="text-sm text-gray-500 font-medium hover:underline"
//             onClick={onCancel}
//           >
//             취소
//           </button>
//         </div>
//       </div>

//       {/* 본문 */}
//       <div className="flex flex-col sm:flex-row gap-6 sm:gap-8 items-start">
//         {/* 왼쪽: 이미지/팔로워 */}
//         <div className="flex flex-col items-center w-full sm:w-32 shrink-0">
//           <div className="relative">
//             <img
//               src={previewUrl}
//               alt="프로필 이미지"
//               className="w-24 h-24 rounded-full object-cover"
//             />
//             <button
//               onClick={handleCameraClick}
//               className="absolute bottom-0 right-0 bg-purple-600 text-white p-1 rounded-full hover:scale-105 transition"
//             >
//               <Camera size={18} />
//             </button>
//           </div>

//           {showImageOptions && (
//             <div className="mt-2 space-y-1 text-sm">
//               <button
//                 onClick={handleImageClick}
//                 className="text-purple-600 hover:underline"
//               >
//                 변경하기
//               </button>
//               {user.imgUrl && user.imgUrl !== DEFAULT_IMG && (
//                 <button
//                   onClick={handleResetToDefaultImage}
//                   className="text-gray-500 hover:underline"
//                 >
//                   기본 이미지로 변경
//                 </button>
//               )}
//             </div>
//           )}
//           <input
//             ref={fileInputRef}
//             type="file"
//             accept="image/*"
//             onChange={handleImageChange}
//             className="hidden"
//           />

//           <div className="mt-4 flex gap-10 sm:gap-6 text-center">
//             <div>
//               <div className="text-lg font-bold">
//                 {user.followerCount?.toLocaleString() ?? "0"}
//               </div>
//               <div className="text-xs text-gray-500">팔로워</div>
//             </div>
//             <div>
//               <div className="text-lg font-bold">
//                 {user.followingCount?.toLocaleString() ?? "0"}
//               </div>
//               <div className="text-xs text-gray-500">팔로잉</div>
//             </div>
//           </div>
//         </div>

//         {/* 오른쪽: 폼 (반응형 레이블-값) */}
//         <div className="min-w-0 flex-1 text-sm grid gap-4">
//           {/* 이메일(읽기전용) */}
//           <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-center gap-2 sm:gap-3">
//             <label className="text-gray-500 font-medium">이메일</label>
//             <div className="min-w-0">
//               <input
//                 value={user.email}
//                 readOnly
//                 className="w-full rounded-lg border border-gray-200 text-gray-500 bg-gray-50 px-2 py-1"
//               />
//             </div>
//           </div>

//           {/* 아이디(읽기전용) */}
//           <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-center gap-2 sm:gap-3">
//             <label className="text-gray-500 font-medium">아이디</label>
//             <div className="min-w-0">
//               <input
//                 value={user.userId}
//                 readOnly
//                 className="w-full rounded-lg border border-gray-200 text-gray-500 bg-gray-50 px-2 py-1"
//               />
//             </div>
//           </div>

//           {/* 닉네임 */}
//           <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-center gap-2 sm:gap-3">
//             <label htmlFor="nickname" className="text-gray-500 font-medium">
//               닉네임
//             </label>
//             <div className="min-w-0">
//               <input
//                 id="nickname"
//                 className="w-full rounded-lg border border-gray-200 px-2 py-1"
//                 value={nickname}
//                 onChange={(e) => setNickname(e.target.value)}
//               />
//             </div>
//           </div>

//           {/* 일반 로그인만 비밀번호 변경 표시 */}
//           {!isSocial ? (
//             <>
//               {/* 새 비밀번호 */}
//               <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-start gap-2 sm:gap-3">
//                 <label className="text-gray-500 font-medium pt-2">
//                   새 비밀번호
//                 </label>
//                 <div className="min-w-0 flex-1">
//                   <input
//                     type="password"
//                     className="w-full rounded-lg border border-gray-200 px-2 py-1"
//                     value={newPassword}
//                     onChange={handleNewPasswordChange}
//                     placeholder="영문/숫자/특수문자 포함, 8자 이상"
//                   />
//                   {newPasswordError && (
//                     <p className="text-red-500 text-xs mt-1">
//                       {newPasswordError}
//                     </p>
//                   )}
//                 </div>
//               </div>

//               {/* 비밀번호 확인 */}
//               <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-start gap-2 sm:gap-3">
//                 <label className="text-gray-500 font-medium pt-2">
//                   비밀번호 확인
//                 </label>
//                 <div className="min-w-0 flex-1">
//                   <input
//                     type="password"
//                     className="w-full rounded-lg border border-gray-200 px-2 py-1"
//                     value={confirmPassword}
//                     onChange={handleConfirmPasswordChange}
//                   />
//                   {confirmPasswordError && (
//                     <p className="text-red-500 text-xs mt-1">
//                       {confirmPasswordError}
//                     </p>
//                   )}
//                 </div>
//               </div>
//             </>
//           ) : (
//             <div className="text-xs text-gray-500">
//               소셜 로그인 계정은 여기서 비밀번호를 변경할 수 없습니다.
//             </div>
//           )}
//         </div>
//       </div>
//     </div>
//   );
// };

// export default EditProfileCard;

import { useState, useRef } from "react";
import type { MyUser } from "../../../types/mypage";
import { fetchMyProfile, updateUserProfile } from "../../../api/userService";
import { Camera } from "lucide-react";
import { useUserStore } from "../../../store/useUserStore";

export type EditProfileCardProps = {
  user: MyUser;
  onCancel: () => void;
  onUpdate: (updatedUser: MyUser) => void;
};

const DEFAULT_IMG = "/default_image.png";

// 영문/숫자/특수문자 각 1개 이상 + 공백 불가 + 8자 이상
const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,}$/;

const EditProfileCard = ({ user, onCancel, onUpdate }: EditProfileCardProps) => {
  const [nickname, setNickname] = useState(user.nickname);
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>(user.imgUrl ?? DEFAULT_IMG);
  const [showImageOptions, setShowImageOptions] = useState(false);
  const [, setDidPickNewImage] = useState(false);

  // 소셜 로그인 여부 (true면 소셜)
  const isSocial = !!(user as any).socialLogin;

  // 비밀번호 관련 상태 (일반 로그인 사용자에게만 의미 있음)
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [newPasswordError, setNewPasswordError] = useState("");
  const [confirmPasswordError, setConfirmPasswordError] = useState("");

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setProfileImage(file);
      setPreviewUrl(URL.createObjectURL(file));
      setShowImageOptions(false);
      setDidPickNewImage(true);
    }
  };

  const handleResetToDefaultImage = () => {
    setPreviewUrl(DEFAULT_IMG);
    setProfileImage(null); // 파일 선택 취소
    setShowImageOptions(false);
    setDidPickNewImage(false);
    // ⚠️ 서버에 실제 삭제 플래그를 보내려면 백엔드 스펙 필요.
    // 현재는 로컬 미리보기만 기본으로 전환.
  };

  const handleCameraClick = () => {
    if (previewUrl === DEFAULT_IMG || user.imgUrl === null) {
      fileInputRef.current?.click();
    } else {
      setShowImageOptions(!showImageOptions);
    }
  };

  const handleSubmit = async () => {
    // 소셜 로그인 계정은 비밀번호 검증 자체를 생략
    if (!isSocial) {
      if (newPassword) {
        if (!PASSWORD_REGEX.test(newPassword)) {
          setNewPasswordError("영문, 숫자, 특수문자를 각각 1자 이상 포함하고 최소 8자여야 합니다.");
          return;
        } else {
          setNewPasswordError("");
        }
        if (newPassword !== confirmPassword) {
          setConfirmPasswordError("새 비밀번호와 확인이 일치하지 않습니다.");
          return;
        } else {
          setConfirmPasswordError("");
        }
      }
    }

    // ─────────────────────────────────────────────
    // FormData: 바뀐 값만, 그리고 profileImg는 "선택했을 때만" 포함
    // ─────────────────────────────────────────────
    const fd = new FormData();

    const trimmedNick = nickname.trim();
    if (trimmedNick && trimmedNick !== user.nickname) {
      fd.append("nickname", trimmedNick);
    }

    // (필요시) 언어 필드 – 고정값이 꼭 필요한 스펙이면 유지
    // fd.append("language", "ko");

    if (!isSocial && newPassword) {
      fd.append("newPassword", newPassword);
    }

    if (profileImage && profileImage.size > 0) {
      fd.append("profileImg", profileImage);
    }
    // ⚠️ 파일을 고르지 않았다면 profileImg는 절대 append하지 않음
    //    (서버가 기본이미지로 리셋하는 문제 방지)

    try {
      await updateUserProfile(fd);

      // 항상 서버 최신값으로 동기화 (이미지/닉/기타 모두 반영)
      const refreshed = await fetchMyProfile();

      // 서버가 일시적으로 빈 값/누락을 줄 때 이전 이미지를 보존
      const prevImg = (user as any)?.imgUrl;
      const incomingImg = (refreshed as any)?.imgUrl;
      const safeImg =
        incomingImg && String(incomingImg).trim() !== "" ? incomingImg : prevImg ?? incomingImg;

      const next: MyUser = { ...refreshed, imgUrl: safeImg } as MyUser;

      // 전역 스토어 동기화
      useUserStore.getState().setMyUser({
        ...next,
        artistList: next.artistList ?? [],
      });

      // 상위로 반영
      onUpdate(next);

      // 비밀번호 입력 초기화
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      console.error(err);
      alert("프로필 수정 중 오류가 발생했습니다.");
    }
  };

  const handleNewPasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setNewPassword(val);

    if (val && !PASSWORD_REGEX.test(val)) {
      setNewPasswordError("영문, 숫자, 특수문자를 각각 1자 이상 포함하고 최소 8자여야 합니다.");
    } else {
      setNewPasswordError("");
    }

    if (confirmPassword && val !== confirmPassword) {
      setConfirmPasswordError("비밀번호가 일치하지 않습니다.");
    } else {
      setConfirmPasswordError("");
    }
  };

  const handleConfirmPasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setConfirmPassword(val);

    if (newPassword && newPassword !== val) {
      setConfirmPasswordError("비밀번호가 일치하지 않습니다.");
    } else {
      setConfirmPasswordError("");
    }
  };

  return (
    <div className="bg-white rounded-xl px-4 sm:px-8 py-6 mb-10 w-full max-w-[880px] mx-auto shadow-sm">
      {/* 헤더 */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-lg font-bold">프로필 수정</h1>
        <div className="space-x-2">
          <button
            className="text-sm text-purple-600 font-medium hover:underline transition"
            onClick={handleSubmit}
          >
            저장
          </button>
          <button
            className="text-sm text-gray-500 font-medium hover:underline"
            onClick={onCancel}
          >
            취소
          </button>
        </div>
      </div>

      {/* 본문 */}
      <div className="flex flex-col sm:flex-row gap-6 sm:gap-8 items-start">
        {/* 왼쪽: 이미지/팔로워 */}
        <div className="flex flex-col items-center w-full sm:w-32 shrink-0">
          <div className="relative">
            <img
              src={previewUrl}
              alt="프로필 이미지"
              className="w-24 h-24 rounded-full object-cover"
            />
            <button
              onClick={handleCameraClick}
              className="absolute bottom-0 right-0 bg-purple-600 text-white p-1 rounded-full hover:scale-105 transition"
            >
              <Camera size={18} />
            </button>
          </div>

          {showImageOptions && (
            <div className="mt-2 space-y-1 text-sm">
              <button onClick={handleImageClick} className="text-purple-600 hover:underline">
                변경하기
              </button>
              {user.imgUrl && user.imgUrl !== DEFAULT_IMG && (
                <button
                  onClick={handleResetToDefaultImage}
                  className="text-gray-500 hover:underline"
                >
                  기본 이미지로 변경
                </button>
              )}
            </div>
          )}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            className="hidden"
          />

          <div className="mt-4 flex gap-10 sm:gap-6 text-center">
            <div>
              <div className="text-lg font-bold">
                {user.followerCount?.toLocaleString() ?? "0"}
              </div>
              <div className="text-xs text-gray-500">팔로워</div>
            </div>
            <div>
              <div className="text-lg font-bold">
                {user.followingCount?.toLocaleString() ?? "0"}
              </div>
              <div className="text-xs text-gray-500">팔로잉</div>
            </div>
          </div>
        </div>

        {/* 오른쪽: 폼 (반응형 레이블-값) */}
        <div className="min-w-0 flex-1 text-sm grid gap-4">
          {/* 이메일(읽기전용) */}
          <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-center gap-2 sm:gap-3">
            <label className="text-gray-500 font-medium">이메일</label>
            <div className="min-w-0">
              <input
                value={user.email}
                readOnly
                className="w-full rounded-lg border border-gray-200 text-gray-500 bg-gray-50 px-2 py-1"
              />
            </div>
          </div>

          {/* 아이디(읽기전용) */}
          <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-center gap-2 sm:gap-3">
            <label className="text-gray-500 font-medium">아이디</label>
            <div className="min-w-0">
              <input
                value={user.userId}
                readOnly
                className="w-full rounded-lg border border-gray-200 text-gray-500 bg-gray-50 px-2 py-1"
              />
            </div>
          </div>

          {/* 닉네임 */}
          <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-center gap-2 sm:gap-3">
            <label htmlFor="nickname" className="text-gray-500 font-medium">
              닉네임
            </label>
            <div className="min-w-0">
              <input
                id="nickname"
                className="w-full rounded-lg border border-gray-200 px-2 py-1"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
              />
            </div>
          </div>

          {/* 일반 로그인만 비밀번호 변경 표시 */}
          {!isSocial ? (
            <>
              {/* 새 비밀번호 */}
              <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-start gap-2 sm:gap-3">
                <label className="text-gray-500 font-medium pt-2">새 비밀번호</label>
                <div className="min-w-0 flex-1">
                  <input
                    type="password"
                    className="w-full rounded-lg border border-gray-200 px-2 py-1"
                    value={newPassword}
                    onChange={handleNewPasswordChange}
                    placeholder="영문/숫자/특수문자 포함, 8자 이상"
                  />
                  {newPasswordError && (
                    <p className="text-red-500 text-xs mt-1">{newPasswordError}</p>
                  )}
                </div>
              </div>

              {/* 비밀번호 확인 */}
              <div className="grid grid-cols-1 sm:grid-cols-[8rem,1fr] items-start gap-2 sm:gap-3">
                <label className="text-gray-500 font-medium pt-2">비밀번호 확인</label>
                <div className="min-w-0 flex-1">
                  <input
                    type="password"
                    className="w-full rounded-lg border border-gray-200 px-2 py-1"
                    value={confirmPassword}
                    onChange={handleConfirmPasswordChange}
                  />
                  {confirmPasswordError && (
                    <p className="text-red-500 text-xs mt-1">{confirmPasswordError}</p>
                  )}
                </div>
              </div>
            </>
          ) : (
            <div className="text-xs text-gray-500">소셜 로그인 계정은 여기서 비밀번호를 변경할 수 없습니다.</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default EditProfileCard;
