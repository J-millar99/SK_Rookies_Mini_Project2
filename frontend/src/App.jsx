import React, { useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { fetchIdentityInfo } from './features/auth/authThunks';

import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import SignUpPage from './pages/SignUpPage';
import LoginPage from './pages/LoginPage';
import MyPage from './pages/MyPage';
import ProductDetailPage from './pages/ProductDetailPage';
import ProductRegisterPage from './pages/ProductRegisterPage';
import MyProductsPage from './pages/MyProductsPage';
import AdminUserPage from './pages/AdminUserPage';
import ProtectedRoute from './components/ProtectedRoute';

import Header from './components/Header';
import NotificationList from './components/NotificationList';
import { Box } from '@mui/material';
import './App.css';

function App() {
  const dispatch = useDispatch();

  useEffect(() => {
    if (localStorage.getItem('accessToken')) {
      dispatch(fetchIdentityInfo());
    }
  }, [dispatch]);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '1000vh' }}>
      <Header />
      <Box component="main" sx={{ flexGrow: 1, mt: '10px', px: 0 }}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignUpPage />} />
          <Route path="/users/notify" element={<NotificationList />} />
          <Route
            path="/users/profile"
            element={
              <ProtectedRoute>
                <MyPage />
              </ProtectedRoute>
            }
          />
          <Route path="/products/:product_id" element={<ProductDetailPage />} />
          <Route path="/my-products/:id/edit" element={<ProductRegisterPage editMode />} />
          <Route path="/my-products" element={<MyProductsPage />} />
          <Route
            path="/products/register"
            element={
              <ProtectedRoute>
                <ProductRegisterPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminUserPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </Box>
    </Box>
  );
}

export default App;
