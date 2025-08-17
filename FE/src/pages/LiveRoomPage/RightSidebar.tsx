import ChatPanel from "./ChatPanel";
import PlaylistPanel from "./PlaylistPanel";
import type { ChatMessage } from "../../types/chat";

type RightSidebarProps = {
  selectedTab: "chat" | "playlist";
  isHost: boolean;
  roomId: string | undefined;
  messages: ChatMessage[];
  sendMessage: (content: string) => void;
  playlist: string[];
  currentVideoIndex: number;
  onAddToPlaylist: (videoId: string) => void;
  onSelectPlaylistIndex?: (index: number) => void;
  onBlockUser: (userId: string) => void;
  onReorderPlaylist?: (from: number, to: number) => void;
  onDeletePlaylistItem?: (index: number) => void;
};

const RightSidebar = ({
  selectedTab,
  isHost,
  // roomId, // (필요시 유지)
  messages,
  sendMessage,
  playlist,
  currentVideoIndex,
  onAddToPlaylist,
  onSelectPlaylistIndex,
  onBlockUser,
  onReorderPlaylist,
  onDeletePlaylistItem,
}: RightSidebarProps) => {
  return (
    <div className="flex-grow flex flex-col bg-gray-800 min-h-0">
      {selectedTab === "chat" ? (
        <ChatPanel
          messages={messages}
          sendMessage={sendMessage}
          onBlockUser={onBlockUser}
        />
      ) : (
        <PlaylistPanel
          isHost={isHost}
          playlist={playlist}
          currentVideoIndex={currentVideoIndex}
          onAddToPlaylist={onAddToPlaylist}
          onSelect={onSelectPlaylistIndex}
          onReorder={onReorderPlaylist}
          onDeleteItem={onDeletePlaylistItem}
        />
      )}
    </div>
  );
};

export default RightSidebar;
