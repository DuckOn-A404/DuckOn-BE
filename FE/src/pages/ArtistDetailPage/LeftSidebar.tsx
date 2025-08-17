import { useNavigate } from "react-router-dom";
import { useArtistFollowStore } from "../../store/useArtistFollowStore";
import { createSlug } from "../../utils/slugUtils";

const LeftSidebar = () => {
  const navigate = useNavigate();
  const { followedArtists } = useArtistFollowStore();

  const handleArtistClick = (artistId: number, nameEn: string) => {
    const slug = createSlug(nameEn);

    navigate(`/artist/${slug}`, { state: { artistId } });
  };

  return (
    <aside className="w-72 p-4">
      <div className="bg-white rounded-2xl shadow p-4 flex flex-col h-[calc(100vh-6rem)]">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-bold">팔로우한 아티스트</h2>
        </div>

        {followedArtists.length > 0 ? (
          // 팔로우한 아티스트가 있을 때
          <>
            <ul className="space-y-2 text-sm mb-4 flex-grow overflow-y-auto custom-scrollbar">
              {followedArtists.map((artist) => {
                return (
                  <li
                    key={artist.artistId}
                    className="flex items-center gap-3 p-2 rounded-lg cursor-pointer hover:bg-gray-100"
                    onClick={() =>
                      handleArtistClick(artist.artistId, artist.nameEn)
                    }
                  >
                    <img
                      src={artist.imgUrl || "https://placehold.co/32x32"}
                      alt={artist.nameKr}
                      className="w-8 h-8 rounded-full object-cover"
                    />
                    <div className="flex-1 flex justify-between items-center">
                      <span className="font-medium">{artist.nameKr}</span>
                    </div>
                  </li>
                );
              })}
            </ul>
            <button
              className="flex-shrink-0 w-full flex items-center justify-center gap-2 bg-purple-600 text-white text-sm font-semibold py-2.5 rounded-lg shadow-sm transition-all duration-300 ease-in-out hover:bg-purple-700 hover:shadow-md hover:-translate-y-0.5"
              onClick={() => navigate("/artist-list")}
            >
              <span>아티스트 더보기</span>
            </button>
          </>
        ) : (
          // 팔로우한 아티스트가 없을 때
          <div className="flex flex-col items-center justify-center text-center flex-grow">
            <p className="text-sm text-gray-500 mb-4">
              팔로우한 아티스트가 없습니다.
            </p>
            <button
              className="mt-auto w-full flex items-center justify-center gap-2 bg-purple-600 text-white text-sm font-semibold py-2.5 rounded-lg shadow-sm transition-all duration-300 ease-in-out hover:bg-purple-700 hover:shadow-md hover:-translate-y-0.5"
              onClick={() => navigate("/artist-list")}
            >
              <span>아티스트 팔로우 하러가기</span>
            </button>
          </div>
        )}
      </div>
    </aside>
  );
};

export default LeftSidebar;
