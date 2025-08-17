import { useState, useEffect, useRef } from "react";
import { type Artist } from "../types/artist";
import { getArtistList, type SortKey, type SortOrder } from "../api/artistService";

type ListParams = {
  q?: string;       // 검색어(옵션) - 제공되면 /artists?keyword= 로 전송됨
  sort: SortKey;    // "followers" | "name" | "debut"
  order: SortOrder; // "asc" | "desc"
  size: number;     // 페이지 당 아이템 수
};

export const useArtistList = (params: ListParams) => {
  const [artists, setArtists] = useState<Artist[]>([]);
  const [totalCount, setTotalCount] = useState<number>(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);

  // IntersectionObserver의 연속 트리거/중복요청을 막기 위한 플래그
  const inflightRef = useRef(false);

  const reset = () => {
    setArtists([]);
    setPage(1);
    setHasMore(true);
    setTotalCount(0);
  };

  const fetchPage = async (p: number) => {
    if (inflightRef.current) return;
    inflightRef.current = true;
    setLoading(true);
    try {
      const res = await getArtistList({
        page: p,
        size: params.size,
        sort: params.sort,
        order: params.order,
        keyword: params.q?.trim() || undefined,
      });

      const newData = res.artistList as Artist[];
      setArtists(prev => (p === 1 ? newData : [...prev, ...newData]));
      setTotalCount(res.totalElements);
      setHasMore(p < res.totalPages); // 검색(keyword) 시 백엔드가 totalPages=1 → hasMore=false
      setPage(p + 1);
    } finally {
      setLoading(false);
      inflightRef.current = false;
    }
  };

  const fetchMore = () => {
    if (!loading && hasMore && !inflightRef.current) {
      fetchPage(page);
    }
  };

  // q/sort/order/size 변경 시 목록 초기화 후 1페이지 로드
  useEffect(() => {
    reset();
    fetchPage(1);
  }, [params.q, params.sort, params.order, params.size]);

  return { artists, totalCount, fetchMore, loading, hasMore, reset };
};
