import axios from "axios";

// 인터셉터 등록
axios.interceptors.request.use((config) => {
  const url = config.url || '';
  const isGetProducts =
    config.method === 'get' &&
    (
      url.startsWith('/api/products') || // 상대경로
      url.match(/^https?:\/\/[^/]+\/api\/products/) // 절대경로
    );
  if (isGetProducts) {
    config.headers = config.headers || {};
    // Authorization 필드를 무조건 제거
    delete config.headers.Authorization;
    // 디버깅 로그
    if (typeof window !== 'undefined') {
      console.debug('[axios] Authorization header forcibly removed for public products API:', url);
    }
    return config;
  }
  // 그 외에는 토큰 추가
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});