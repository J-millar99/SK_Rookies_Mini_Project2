import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Container, Grid, Typography } from '@mui/material';
import ProductCard from '../components/ProductCard';
import InfiniteScroll from 'react-infinite-scroll-component';
import Layout from '../components/Layout';
const allProducts = Array.from({ length: 100 }).map((_, i) => ({
  id: i + 1,
  title: `상품 ${i + 1}`,
  price: Math.floor(Math.random() * 100000) + 10000,
  image: 'https://via.placeholder.com/200',
}));

const HomePage = () => {
  const [products, setProducts] = useState(allProducts.slice(0, 12));
  const [hasMore, setHasMore] = useState(true);

  const navigate = useNavigate();
    const fetchMoreData = () => {
    if (products.length >= allProducts.length) {
      setHasMore(false);
      return;
    }

    // 다음 상품 8개씩 로드
    const next = allProducts.slice(products.length, products.length + 8);
    setTimeout(() => {
      setProducts(prev => [...prev, ...next]);
    }, 500); // 로딩 느낌
  };

  // 토큰 값 유무 검사
  const handleRegisterClick = () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      alert("로그인을 진행해주세요!");
      return;
    }
    navigate('/products/register');
  };

  return (
    <Layout>
    <Container sx={{ paddingTop: '80px' }}>
      <Button
        variant="contained"
        color="success"
        sx={{ ml: 2 }}
        onClick={handleRegisterClick}
      >
        상품 등록
      </Button>
      <Button
        variant="contained"
        color="primary"
        sx={{ ml: 2 }}
        onClick={() => navigate('/my-products')}
      >
        내가 올린 상품
      </Button>
      <Typography variant="h5" sx={{ mt: 4, mb: 2 }}>오늘의 상품 추천</Typography>
        <InfiniteScroll
          dataLength={products.length}
          next={fetchMoreData}
          hasMore={hasMore}
          loader={<Typography>로딩 중...</Typography>}
          endMessage={<Typography sx={{ textAlign: 'center', mt: 4 }}>모든 상품을 다 불러왔어요!</Typography>}
        >
          <Grid container spacing={2}>
            {products.map(product => (
              <Grid item key={product.id} xs={6} sm={4} md={3}>
                <ProductCard product={product} />
              </Grid>
            ))}
          </Grid>
        </InfiniteScroll>
      <Button variant="contained" onClick={() => navigate('/register')}>
        회원가입1
      </Button>
      <Button variant="contained" onClick={() => navigate('/signup')}>
        회원가입2
      </Button>
    </Container>
    </Layout>
  );
};

export default HomePage;
