import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import ProductSearch from './ProductSearch';

const Header = () => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate('/'); // 로그아웃 후 홈페이지로 이동 (원하는 경로로 변경 가능)
  };

  return (
    <>
      {/* 최상단 바: 비움 */}
      <AppBar position="static" color="default" elevation={0} sx={{ height: 0 }} />

      {/* 고정 헤더 */}
      <AppBar
        position="sticky"
        color="primary"
        elevation={1}
        sx={{ top: 0, zIndex: (theme) => theme.zIndex.appBar }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Typography
            variant="h5"
            component="div"
            onClick={() => navigate('/')}
            sx={{
              cursor: 'pointer',
              display: 'flex',
              fontWeight: 700,
              color: '#FA5',
              textShadow: '0 1px 4px rgba(0,0,0,0.25)',
            }}
          >
            <Box
              component="img"
              src="/android-chrome-192x192.png"
              alt="루키장터 로고"
              sx={{ width: 36, height: 36, mr: 1 }}
            />
            루키장터
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <ProductSearch />
            {isAuthenticated ? (
              <>
                <Button color="inherit" onClick={handleLogout}>
                  로그아웃
                </Button>
                <Button color="inherit" onClick={() => navigate('/users/profile')}>
                  마이페이지
                </Button>
              </>
            ) : (
              <>
                <Button color="inherit" onClick={() => navigate('/login')}>
                  로그인
                </Button>
                <Button color="inherit" onClick={() => navigate('/signup')}>
                  회원가입
                </Button>
              </>
            )}
          </Box>
        </Toolbar>
      </AppBar>
    </>
  );
};

export default Header;
