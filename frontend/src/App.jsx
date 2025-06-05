import React, { useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { fetchIdentityInfo } from "./features/auth/authThunks";

import HomePage from './pages/HomePage';
import RegisterPage from './pages/RegisterPage';
import ProductsPage from './pages/ProductsPage';
import SignUpPage from './pages/SignUpPage';
import LoginPage from './pages/LoginPage';
import MyPage from './pages/MyPage';
import ProductDetailPage from './pages/ProductDetailPage';
import ProductRegisterPage from './pages/ProductRegisterPage';
import MyProductsPage from "./pages/MyProductsPage";
import MyProductDetailPage from "./pages/MyProductDetailPage";

import Header from './components/Header';
import { Box } from '@mui/material';
import './App.css';

function App() {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(fetchIdentityInfo());
  }, [dispatch]);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '1000vh' }}>
      <Header />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          mt: '10px',
          px: 0,
        }}
      >
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignUpPage />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/products/:product_id" element={<ProductDetailPage />} />
          <Route path="/products/register" element={<ProductRegisterPage />} />
          <Route path="/my-products/:id/edit" element={<ProductRegisterPage editMode />} />
          <Route path="/my-products" element={<MyProductsPage />} />
          <Route path="/my-products/:productId" element={<MyProductDetailPage />} />
        </Routes>
      </Box>
    </Box>
  );
}

export default App;
