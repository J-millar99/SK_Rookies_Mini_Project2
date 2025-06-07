import React, { useState, useEffect } from 'react';
import { Button, Container, Grid, Typography, Divider } from '@mui/material';
import ProductCard from '../components/ProductCard';
import InfiniteScroll from 'react-infinite-scroll-component';
import Layout from '../components/Layout';
import axios from 'axios';
import { FormatTime } from '../utils/FormatTime';
import { useLocation } from 'react-router-dom';
import FormSnackbar from '../components/FormSnackbar';

const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(0); // 현재 페이지 번호
  const [loading, setLoading] = useState(true); // 초기 로딩 상태
  const token = localStorage.getItem('accessToken');

  // snackbar 상태
  const location = useLocation();
  const snackbarState = location.state?.snackbar;
  const [snackbar, setSnackbar] = useState(
    snackbarState || { open: false, message: '', severity: 'info' }
  );

  // location.state로 온 snackbar 메시지는 한 번만 보여주고 지워줌
  useEffect(() => {
    if (snackbarState) {
      setSnackbar(snackbarState);
      window.history.replaceState({}, document.title);
    }
  }, [snackbarState]);

  // 초기 상품 데이터를 불러오는 useEffect
  useEffect(() => {
    window.scrollTo(0, 0);
    setProducts([]);
    setPage(0);
    setHasMore(true);
    setLoading(true); // 로딩 시작
    fetchMoreData(0); // 첫 페이지 데이터 불러오기
    // eslint-disable-next-line
  }, []);

  // 상품 데이터를 페이지네이션으로 불러오는 함수
  const fetchMoreData = async (currentPage) => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/products?page=${currentPage}&size=12&sort=createdAt,desc`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const { content, pagination } = response.data.data;

      setProducts((prevProducts) => {
        // 1. 기존 상품들을 Map에 추가하여 ID를 키로 매핑
        const uniqueProductsMap = new Map(prevProducts.map((product) => [product.id, product]));

        // 2. 새로 받아온 상품들을 Map에 추가.
        //    ID가 중복되면 새로운 상품 정보로 덮어씌워짐.
        content.forEach((newProduct) => {
          uniqueProductsMap.set(newProduct.id, newProduct);
        });

        // 3. Map의 값들만 다시 배열로 만들어 반환
        return Array.from(uniqueProductsMap.values());
      });

      setHasMore(!pagination.last);
      setPage(currentPage + 1);
    } catch (error) {
      console.error('상품 목록을 불러오는 데 실패했습니다.', error);
      setHasMore(false);
    } finally {
      setLoading(false);
    }
  };

  // InfiniteScroll의 next prop으로 전달될 함수
  const handleLoadMore = () => {
    if (!loading && hasMore) {
      fetchMoreData(page);
    }
  };

  return (
    <Layout>
      <Container >
        <Typography variant="h5" fontWeight={700} sx={{ mb: 2 }}>
          오늘의 상품 추천
        </Typography>
        <Divider sx={{ mb: 4, borderColor: "#222", borderWidth: 2 }} />
        {loading && products.length === 0 ? (
          <Typography sx={{ textAlign: 'center', mt: 4 }}>상품을 불러오는 중입니다...</Typography>
        ) : (
          <InfiniteScroll
            dataLength={products.length}
            next={handleLoadMore}
            hasMore={hasMore}
            loader={<Typography sx={{ textAlign: 'center', mt: 4 }}>로딩 중...</Typography>}
            endMessage={
              <Typography sx={{ textAlign: 'center', mt: 4 }}>
                모든 상품을 다 불러왔어요!
              </Typography>
            }
          >
            <Grid container spacing={2}>
              {products.map((product) => (
                <Grid item key={product.id} xs={6} sm={4} md={3}>
                  <ProductCard product={product} formatTime={FormatTime} />
                </Grid>
              ))}
            </Grid>
          </InfiniteScroll>
        )}
        {!loading && products.length === 0 && !hasMore && (
          <Typography sx={{ textAlign: 'center', mt: 4 }}>등록된 상품이 없습니다.</Typography>
        )}
        <FormSnackbar
          open={snackbar.open}
          message={snackbar.message}
          severity={snackbar.severity}
          onClose={() => setSnackbar({ ...snackbar, open: false })}
        />
      </Container>
    </Layout>
  );
};

export default HomePage;