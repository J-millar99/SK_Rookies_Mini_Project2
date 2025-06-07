// components/NotificationList.jsx

import React, { useEffect, useState, useRef, useCallback } from 'react';
import {
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  CircularProgress,
  Alert,
  Paper,
  Button,
  Divider,
  Snackbar,
} from '@mui/material';
import {
  getNotifications,
  markNotificationAsRead,
  deleteNotification,
  updateReservationStatus,
} from '../api/notificationService'; // notificationService 임포트
import { FormatTime } from '../utils/FormatTime';

const NotificationList = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(0);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');

  const observer = useRef();
  const lastNotificationElementRef = useCallback(
    (node) => {
      if (loading) return;
      if (observer.current) observer.current.disconnect();
      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          setPage((prevPage) => prevPage + 1);
        }
      });
      if (node) observer.current.observe(node);
    },
    [loading, hasMore]
  );

  const fetchNotifications = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await getNotifications(page);
      if (result.success) {
        setNotifications((prevNotifications) => {
          const newNotifications = result.data.content.filter(
            (newNotif) =>
              !prevNotifications.some(
                (prevNotif) => prevNotif.notificationId === newNotif.notificationId
              )
          );
          return [...prevNotifications, ...newNotifications];
        });
        setHasMore(!result.data.last);
      } else {
        setError(result.error || '알림을 불러오는 데 실패했습니다.');
      }
    } catch (err) {
      console.error('알림 페칭 중 오류 발생:', err);
      setError('알림을 불러오는 중 문제가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const handleNotificationClick = async (notificationId, isRead) => {
    if (!isRead) {
      const result = await markNotificationAsRead(notificationId);
      if (result.success) {
        setNotifications((prevNotifications) =>
          prevNotifications.map((notif) =>
            notif.notificationId === notificationId ? { ...notif, isRead: true } : notif
          )
        );
      } else {
        setSnackbarMessage('읽음 처리 실패: ' + (result.error?.message || '알 수 없는 오류'));
        setSnackbarOpen(true);
      }
    }
  };

  const handleDeleteNotification = async (notificationId) => {
    const confirmDelete = window.confirm('정말로 알림을 삭제하시겠습니까?');
    if (!confirmDelete) return;

    const result = await deleteNotification(notificationId);
    if (result.success) {
      setNotifications((prevNotifications) =>
        prevNotifications.filter((notif) => notif.notificationId !== notificationId)
      );
      setSnackbarMessage('알림이 삭제되었습니다.');
      setSnackbarOpen(true);
    } else {
      setSnackbarMessage('알림 삭제 실패: ' + (result.error?.message || '알 수 없는 오류'));
      setSnackbarOpen(true);
    }
  };

  // 예약 수락 핸들러
  const handleAcceptReservation = async (notificationId, reservationId) => {
    // buyerName 파라미터 제거
    const confirmAccept = window.confirm('거래 요청을 수락하시겠습니까?'); // 메시지 수정
    if (!confirmAccept) return;

    const result = await updateReservationStatus(reservationId, 'ACCEPTED');
    if (result.success) {
      const readResult = await markNotificationAsRead(notificationId);
      if (readResult.success) {
        setNotifications((prevNotifications) =>
          prevNotifications.map((notif) =>
            notif.notificationId === notificationId
              ? { ...notif, isRead: true, reservationStatus: 'ACCEPTED' }
              : notif
          )
        );
        setSnackbarMessage('예약 요청이 수락되었습니다.');
      } else {
        setSnackbarMessage(
          '예약 수락은 완료되었으나 알림 읽음 처리 실패: ' +
            (readResult.error?.message || '알 수 없는 오류')
        );
      }
      setSnackbarOpen(true);
    } else {
      setSnackbarMessage('예약 수락 실패: ' + (result.error?.message || '알 수 없는 오류'));
      setSnackbarOpen(true);
    }
  };

  // 예약 거절 핸들러
  const handleDeclineReservation = async (notificationId, reservationId) => {
    // buyerName 파라미터 제거
    const confirmDecline = window.confirm('거래 요청을 거절하시겠습니까?'); // 메시지 수정
    if (!confirmDecline) return;

    const result = await updateReservationStatus(reservationId, 'DECLINED');
    if (result.success) {
      const readResult = await markNotificationAsRead(notificationId);
      if (readResult.success) {
        setNotifications((prevNotifications) =>
          prevNotifications.map((notif) =>
            notif.notificationId === notificationId
              ? { ...notif, isRead: true, reservationStatus: 'DECLINED' }
              : notif
          )
        );
        setSnackbarMessage('예약 요청이 거절되었습니다.');
      } else {
        setSnackbarMessage(
          '예약 거절은 완료되었으나 알림 읽음 처리 실패: ' +
            (readResult.error?.message || '알 수 없는 오류')
        );
      }
      setSnackbarOpen(true);
    } else {
      setSnackbarMessage('예약 거절 실패: ' + (result.error?.message || '알 수 없는 오류'));
      setSnackbarOpen(true);
    }
  };

  const handleCloseSnackbar = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setSnackbarOpen(false);
  };

  if (error) {
    return (
      <Box sx={{ p: 2 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', mt: 4, p: 2 }}>
      <Typography variant="h4" component="h1" gutterBottom align="center">
        알림 목록
      </Typography>
      {notifications.length === 0 && !loading && !hasMore ? (
        <Paper elevation={1} sx={{ p: 3, textAlign: 'center', mt: 3 }}>
          <Typography variant="h6" color="text.secondary">
            받은 알림이 없습니다.
          </Typography>
        </Paper>
      ) : (
        <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
          {notifications.map((notification, index) => {
            const isLastElement = index === notifications.length - 1;
            const isReservationNotification = notification.entityType === 'Reservation';
            const isReservationHandled = notification.isRead && isReservationNotification;

            return (
              <React.Fragment key={notification.notificationId}>
                <ListItem
                  alignItems="flex-start"
                  sx={{
                    opacity: notification.isRead ? 0.7 : 1,
                    backgroundColor: notification.isRead ? '#f5f5f5' : 'white',
                    cursor: 'pointer',
                    '&:hover': {
                      backgroundColor: notification.isRead ? '#eeeeee' : '#f0f0f0',
                    },
                  }}
                  onClick={
                    isReservationNotification
                      ? undefined
                      : () =>
                          handleNotificationClick(notification.notificationId, notification.isRead)
                  }
                  ref={isLastElement && hasMore ? lastNotificationElementRef : null}
                >
                  <ListItemText
                    primary={
                      <Typography
                        component="span"
                        variant="body1"
                        color="text.primary"
                        sx={{ fontWeight: notification.isRead ? 'normal' : 'bold' }}
                      >
                        {notification.message}
                      </Typography>
                    }
                    secondary={
                      <Typography
                        sx={{ display: 'inline', ml: 2}}
                        component="span"
                        variant="body2"
                        color="text.secondary"
                      >
                        {notification.sentAt ? FormatTime(notification.sentAt) : ''}
                      </Typography>
                    }
                  />
                  <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                    {isReservationNotification ? (
                      // Reservation 타입 알림일 때
                      <>
                        <Button
                          variant="contained"
                          color={
                            isReservationHandled && notification.reservationStatus !== 'ACCEPTED'
                              ? 'inherit'
                              : 'success'
                          }
                          sx={
                            isReservationHandled && notification.reservationStatus !== 'ACCEPTED'
                              ? { backgroundColor: '#bdbdbd' }
                              : {}
                          }
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleAcceptReservation(
                              notification.notificationId,
                              notification.entityId
                            ); // buyerName 파라미터 제거
                          }}
                          disabled={isReservationHandled}
                        >
                          수락
                        </Button>
                        <Button
                          variant="contained"
                          color={
                            isReservationHandled && notification.reservationStatus !== 'DECLINED'
                              ? 'inherit'
                              : 'error'
                          }
                          sx={
                            isReservationHandled && notification.reservationStatus !== 'DECLINED'
                              ? { backgroundColor: '#bdbdbd' }
                              : {}
                          }
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeclineReservation(
                              notification.notificationId,
                              notification.entityId
                            ); // buyerName 파라미터 제거
                          }}
                          disabled={isReservationHandled}
                        >
                          거절
                        </Button>
                      </>
                    ) : (
                      // 그 외 알림 타입일 때
                      <>
                        {!notification.isRead && (
                          <Button
                            variant="outlined"
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleNotificationClick(
                                notification.notificationId,
                                notification.isRead
                              );
                            }}
                          >
                            읽음처리
                          </Button>
                        )}
                        <Button
                          variant="outlined"
                          color="error"
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteNotification(notification.notificationId);
                          }}
                        >
                          삭제
                        </Button>
                      </>
                    )}
                  </Box>
                </ListItem>
                {!isLastElement && <Divider component="li" />}
              </React.Fragment>
            );
          })}
          {loading && (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
              <CircularProgress size={24} />
            </Box>
          )}
          {!hasMore && notifications.length > 0 && (
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', p: 2 }}>
              더 이상 알림이 없습니다.
            </Typography>
          )}
        </List>
      )}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        message={snackbarMessage}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  );
};

export default NotificationList;
