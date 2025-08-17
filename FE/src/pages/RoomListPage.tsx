import {useState, useEffect, useRef} from "react";
import VideoCard from "../components/domain/video/VideoCard";
import VideoCardSkeleton from "../components/domain/video/VideoCardSkeleton";
import {useTrendingRooms} from "../hooks/useTrendingRooms";
import type {trendingRoom} from "../types/room";
import {Tv} from "lucide-react";

const PAGE_SIZE = 24;

const RoomListPage = () => {
    const [page, setPage] = useState(1);
    const [rooms, setRooms] = useState<trendingRoom[]>([]);
    const [hasMore, setHasMore] = useState(true);
    const [totalCount, setTotalCount] = useState(0);

    const sentinelRef = useRef<HTMLDivElement | null>(null);

    const {data, isLoading, error} = useTrendingRooms(page, PAGE_SIZE);

    useEffect(() => {
        if (!data) return;
        // 새 페이지 결과 누적
        setRooms(prev => [...prev, ...data.roomInfoList]);
        setHasMore(data.hasNext);
        setTotalCount(data.totalElements);
    }, [data]);

    // 무한 스크롤
    useEffect(() => {
        if (!hasMore || isLoading) return;

        const io = new IntersectionObserver(
            entries => {
                if (entries[0].isIntersecting) {
                    setPage(prev => prev + 1);
                }
            },
            {threshold: 0.5}
        );

        const el = sentinelRef.current;
        if (el) io.observe(el);

        return () => {
            if (el) io.unobserve(el);
            io.disconnect();
        };
    }, [hasMore, isLoading]);

    return (
        <div className="px-4 md:px-10 py-8">
            {/* 제목 */}
            <div className="text-center py-8 mb-5">
                <h1 className="text-4xl font-extrabold text-gray-800 mb-2">라이브 방송</h1>
                <p className="text-lg text-gray-500">다양한 라이브 방송들을 만나보세요.</p>
            </div>

            {/* 총 개수 */}
            <div className="w-full max-w-md mx-auto mb-8">
                <p className="text-sm text-center mt-2 text-gray-600">총 {totalCount}개 방</p>
            </div>

            {/* 목록 */}
            <section>
                <div className="flex flex-wrap justify-center gap-8 flex-grow">
                    {isLoading && rooms.length === 0 ? (
                        Array.from({length: 8}).map((_, i) => <VideoCardSkeleton key={i} />)
                    ) : error ? (
                        <p className="w-full text-center text-red-500 py-20">{error}</p>
                    ) : rooms.length > 0 ? (
                        rooms.map((r) => (
                            <VideoCard
                                key={r.roomId}
                                roomId={r.roomId}
                                title={r.title}
                                hostId={r.hostId}
                                hostNickname={r.hostNickname}
                                imgUrl={r.imgUrl}
                                participantCount={r.participantCount}
                                hostProfileImgUrl={r.hostProfileImgUrl ?? ""}
                            />
                        ))
                    ) : (
                        <div className="w-full flex flex-col items-center justify-center text-center text-gray-500 py-20 bg-gray-50 rounded-2xl">
                            <Tv size={48} className="text-gray-300 mb-4" />
                            <p className="font-semibold text-gray-600">진행 중인 라이브 방송이 없습니다.</p>
                        </div>
                    )}
                </div>
            </section>

            {/* 무한스크롤 센티넬 */}
            <div ref={sentinelRef} className="h-10 mt-10" />

            {/* 추가 로딩 스피너 (이미 목록이 있을 때) */}
            {isLoading && rooms.length > 0 && (
                <div className="flex justify-center items-center h-24">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600" />
                </div>
            )}
        </div>
    );
};

export default RoomListPage;
