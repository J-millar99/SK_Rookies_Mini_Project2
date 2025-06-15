import React, { useEffect, useMemo, useState } from 'react';
import {
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  Box,
} from '@mui/material';
import axios from 'axios';
import { useDispatch, useSelector } from 'react-redux';
import { setIdentityInfo, clearAuthState } from '../features/auth/authSlice';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import { getAreas } from '../api/area';
import FormSnackbar from '../components/FormSnackbar';
import notificationService from '../api/notificationService';
import { FormatTime } from '../utils/FormatTime';

const MyPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { identityInfo } = useSelector((state) => state.auth);

  const [editing, setEditing] = useState(false);
  const [areas, setAreas] = useState([]);
  const [formData, setFormData] = useState({
    userName: '',
    phone: '',
    areaId: '',
  });

  const [errors, setErrors] = useState({});
  const [password, setPassword] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const [trades, setTrades] = useState([]);
  const [notifications, setNotifications] = useState([]);

  // Snackbar 상태
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info',
  });

  const handleSnackbarClose = () => setSnackbar({ ...snackbar, open: false });

  useEffect(() => {
    window.scrollTo(0, 0);
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const res = await axios.get('http://localhost:8080/api/users/profile', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        dispatch(setIdentityInfo(res.data));
        setFormData({
          userName: res.data.userName,
          phone: res.data.phone,
          areaId: res.data.area?.areaId || '',
        });
      } catch (e) {
        console.error('프로필 불러오기 실패', e);
      }
    };

    const fetchAreas = async () => {
      try {
        const res = await getAreas();
        setAreas(res.data);
      } catch (e) {
        console.error('지역 목록 로딩 실패', e);
      }
    };

    fetchProfile();
    fetchAreas();

    setTrades([]);
    setNotifications([]);
  }, [dispatch]);

  const validateField = (name, value) => {
    let msg = '';
    switch (name) {
      case 'userName':
        if (!value) msg = '이름은 필수입니다.';
        else if (value.length < 2 || value.length > 12) msg = '이름은 2~12자 이내여야 합니다.';
        else if (!/^[가-힣a-zA-Z]+$/.test(value)) msg = '한글 또는 영문만 입력 가능합니다.';
        break;
      case 'phone':
        if (!value) msg = '전화번호는 필수입니다.';
        else if (!/^010-\d{4}-\d{4}$/.test(value)) msg = '010-XXXX-XXXX 형식으로 입력해주세요.';
        break;
      case 'areaId':
        if (!value) msg = '지역을 선택해주세요.';
        break;
      default:
        break;
    }
    return msg;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setErrors((prev) => ({
      ...prev,
      [name]: validateField(name, value),
    }));
  };

  const isFormValid = useMemo(() => {
    return (
      formData.userName &&
      formData.phone &&
      formData.areaId &&
      !Object.values(errors).some((e) => e)
    );
  }, [formData, errors]);

  const handleSave = async () => {
    const newErrors = {};
    Object.keys(formData).forEach((key) => {
      const err = validateField(key, formData[key]);
      if (err) newErrors[key] = err;
    });
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      const token = localStorage.getItem('accessToken');
      const response = await axios.put(
        'http://localhost:8080/api/users/profile',
        {
          userName: formData.userName,
          phone: formData.phone,
          areaId: formData.areaId,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );
      dispatch(setIdentityInfo(response.data));
      setEditing(false);
      setSnackbar({
        open: true,
        message: '프로필이 성공적으로 업데이트되었습니다.',
        severity: 'success',
      });
    } catch (error) {
      console.error('프로필 업데이트 실패:', error);
      setSnackbar({
        open: true,
        message: '프로필 업데이트에 실패했습니다.',
        severity: 'error',
      });
    }
  };

  const handleDeleteAccount = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      await axios.delete('http://localhost:8080/api/auth/delete', {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        data: { password },
      });

      setSnackbar({
        open: true,
        message: '계정이 삭제되었습니다.',
        severity: 'success',
      });
      dispatch(clearAuthState());
      useAuthStore.getState().logout();
      navigate('/login');
    } catch (error) {
      console.error('계정 삭제 실패:', error);
      setSnackbar({
        open: true,
        message: '비밀번호가 일치하지 않아서 삭제에 실패했습니다.',
        severity: 'error',
      });
    }
  };

  // 알림 리스트 불러오기
  useEffect(() => {
    const fetchNotifications = async () => {
      const token = localStorage.getItem('accessToken');
      if (!token) return;
      const res = await notificationService.getNotifications(0, 10, 'createdAt,desc');
      console.log('알림 리스트 응답:', res);
      if (res.success && res.data?.content) {
        setNotifications(res.data.content);
      } else {
        setNotifications([]);
      }
    };
    fetchNotifications();
  }, []);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        마이페이지
      </Typography>
      <Box my={4}>
        <Button
          variant="contained"
          color="success"
          sx={{ ml: 2 }}
          onClick={() => navigate('/products/register')}
        >
          상품 등록
        </Button>
        <Button
          variant="contained"
          color="info"
          sx={{ ml: 2 }}
          onClick={() => navigate('/my-products')}
        >
          My 상품
        </Button>
        <Button
          variant="contained"
          color="secondary"
          sx={{ ml: 2 }}
          onClick={() => navigate('/my-products', { state: { dibs: true } })}
        >
          찜한 상품
        </Button>
        <Button variant="contained" color="error" sx={{ ml: 2 }} onClick={() => navigate('/chats')}>
          내 채팅
        </Button>
      </Box>

      {editing ? (
        <>
          <TextField
            fullWidth
            label="이름"
            name="userName"
            value={formData.userName}
            onChange={handleChange}
            error={!!errors.userName}
            helperText={errors.userName}
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            label="전화번호"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            error={!!errors.phone}
            helperText={errors.phone}
            sx={{ mb: 2 }}
          />
          <FormControl fullWidth error={!!errors.areaId} sx={{ mb: 2 }}>
            <InputLabel>지역</InputLabel>
            <Select name="areaId" value={formData.areaId} onChange={handleChange} label="지역">
              {areas.map((area) => (
                <MenuItem key={area.areaId} value={area.areaId}>
                  {area.areaName}
                </MenuItem>
              ))}
            </Select>
            {errors.areaId && (
              <Typography variant="caption" color="error">
                {errors.areaId}
              </Typography>
            )}
          </FormControl>
          <Button variant="contained" onClick={handleSave} disabled={!isFormValid}>
            저장
          </Button>{' '}
          <Button variant="outlined" onClick={() => setEditing(false)}>
            취소
          </Button>
        </>
      ) : (
        <>
          <Typography>이름: {identityInfo?.userName}</Typography>
          <Typography>전화번호: {identityInfo?.phone}</Typography>
          <Typography>지역: {identityInfo?.area?.areaName}</Typography>
          <Button variant="contained" sx={{ mt: 2 }} onClick={() => setEditing(true)}>
            수정
          </Button>
        </>
      )}

      <hr style={{ margin: '30px 0' }} />

      {!showDeleteConfirm ? (
        <Button onClick={() => setShowDeleteConfirm(true)} color="error">
          계정 삭제
        </Button>
      ) : (
        <Box sx={{ mt: 2 }}>
          <Typography>계정을 삭제하려면 비밀번호를 입력하세요:</Typography>
          <TextField
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호"
            sx={{ my: 2, maxWidth: '400px', width: '100%' }}
          />
          <br />
          <Button onClick={handleDeleteAccount} color="error" variant="contained">
            삭제 확인
          </Button>{' '}
          <br />
          <Button onClick={() => setShowDeleteConfirm(false)}>취소</Button>
        </Box>
      )}

      <hr style={{ margin: '30px 0' }} />

      {/* 현재 진행 중인 거래 */}
      <Typography variant="h5" gutterBottom>
        현재 진행 중인 거래
      </Typography>
      {trades.length > 0 ? (
        trades.map((trade) => (
          <Box key={trade.id} sx={{ mb: 1, p: 1, border: '1px solid #ccc', borderRadius: 2 }}>
            <Typography>{trade.title}</Typography>
            <Typography variant="caption" color="text.secondary">
              상태: {trade.status}
            </Typography>
          </Box>
        ))
      ) : (
        <Typography color="text.secondary">진행 중인 거래가 없습니다.</Typography>
      )}

      <hr style={{ margin: '30px 0' }} />

      {/* 알림 리스트 */}
      <Typography variant="h5" gutterBottom>
        알림 리스트
      </Typography>
      {notifications.length > 0 ? (
        notifications.map((noti) => (
          <Box
            key={noti.notificationId || noti.id}
            sx={{ mb: 1, p: 1, border: '1px solid #ccc', borderRadius: 2 }}
          >
            <Typography>{noti.message}</Typography>
            <Typography variant="caption" color="text.secondary">
              {noti.createdAt ? FormatTime(noti.createdAt) : noti.date || noti.createdAt}
            </Typography>
          </Box>
        ))
      ) : (
        <Typography color="text.secondary">알림이 없습니다.</Typography>
      )}

      {/* Snackbar 팝업 */}
      <FormSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={handleSnackbarClose}
      />
    </Box>
  );
};

export default MyPage;
