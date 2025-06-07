import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import ProductSearch from './ProductSearch';
import { useSelector } from 'react-redux';
import { getUnreadNotificationsCount } from '../api/notificationService';
import { useEffect, useState } from 'react';


const Header = () => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuthStore();

  // Redux에서 사용자 정보 및 로딩 상태 가져오기
  const identityInfo = useSelector((state) => state.auth.identityInfo);

  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    // 읽지 않은 알림 개수를 가져오는 비동기 함수
    const fetchUnreadCount = async () => {
      if (!isAuthenticated) {
        setUnreadCount(0); // 로그아웃 상태면 0으로 초기화
        return;
      }
      try {
        const response = await getUnreadNotificationsCount();
        if (response.success) {
          setUnreadCount(response.data);
        } else {
          console.error('읽지 않은 알림 수를 가져오는데 실패했습니다:', response.error);
          setUnreadCount(0); // 실패 시에도 0으로 설정
        }
      } catch (error) {
        console.error('읽지 않은 알림 수를 가져오는 중 오류 발생:', error);
        setUnreadCount(0); // 오류 발생 시에도 0으로 설정
      }
    };

    fetchUnreadCount(); // 컴포넌트 마운트 시 또는 isAuthenticated 변경 시 즉시 호출

    // 주기적으로 알림 개수를 업데이트하려면 다음 주석을 해제하고 사용
    // const intervalId = setInterval(fetchUnreadCount, 60000); // 1분(60000ms)마다 업데이트
    // return () => clearInterval(intervalId); // 컴포넌트 언마운트 시 인터벌 해제
  }, [isAuthenticated]); // isAuthenticated 상태가 변경될 때마다 실행

  const handleLogout = () => {
    logout();
    navigate('/');
  };
  // 로그인되어 있지만 사용자 정보가 아직 없을 때는 렌더링 일시 중단
  // if (isAuthenticated && !identityInfo && !loading) {
  //   return null;
  // }

  return (
    <>
      {/* 최상단 알림 영역 (새로 추가) */}
      <AppBar
        position="static"
        color="transparent"
        elevation={0}
        sx={{ height: 10, justifyContent: 'flex-end', zIndex: (theme) => theme.zIndex.drawer + 1 }}
      >
        <Toolbar variant="dense" sx={{ minHeight: 40, justifyContent: 'flex-end', pr: 2 }}>
          {isAuthenticated && ( // 로그인 상태일 때만 알림 표시
            <Button
              color="inherit"
              onClick={() => navigate('/users/notify')}
              sx={{
                fontWeight: 'bold',
                minWidth: 'auto',
                p: '4px 8px',
                fontSize: '0.85rem',
                color: 'text.secondary',
                '&:hover': {
                  color: 'text.primary', // 호버 시 색상 변경
                  backgroundColor: 'rgba(0, 0, 0, 0.04)', // 호버 시 배경색 변경
                },
              }}
            >
              알림 {unreadCount > 0 && `(${unreadCount})`}
            </Button>
          )}
        </Toolbar>
      </AppBar>

      {/* 기존 메인 헤더바 */}
      <AppBar
        position="sticky"
        color="primary"
        elevation={1}
        sx={{ top: 0, zIndex: (theme) => theme.zIndex.appBar, whiteSpace: 'nowrap' }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Typography
            variant="h5"
            component="div"
            onClick={() => navigate('/')}
            color = 'secondary'
            sx={{
              cursor: 'pointer',
              display: 'flex',
              fontWeight: 700,
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
                {identityInfo?.isAdmin ? (
                  <Button color="inherit" onClick={() => navigate('/admin')}>
                    관리자 페이지
                  </Button>
                ) : (
                  <Button color="inherit" onClick={() => navigate('/users/profile')}>
                    마이페이지
                  </Button>
                )}
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
