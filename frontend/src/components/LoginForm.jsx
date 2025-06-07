import React, { useState, useMemo } from 'react';
import { TextField, Button, Box, Typography } from '@mui/material';
import { loginUser } from '../api/auth';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore'; // Zustand
import axios from 'axios';
import FormSnackbar from './FormSnackbar';
import { useDispatch } from 'react-redux';
import { fetchIdentityInfo } from '../features/auth/authThunks';

const LoginForm = () => {
  const [formData, setFormData] = useState({
    loginId: '',
    password: '',
  });

  const [errors, setErrors] = useState({
    loginId: '',
    password: '',
    submit: '',
  });

  const [loading, setLoading] = useState(false);

  // snackbar 상태 추가
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });

  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { login } = useAuthStore(); // Zustand

  const validateField = (name, value) => {
    let message = '';
    switch (name) {
      case 'loginId':
        if (!value) {
          message = '아이디를 입력해주세요.';
        } else if (value.length < 4 || value.length > 20) {
          message = '아이디는 4~20자 이내로 입력해야 합니다.';
        } else if (!/^[a-zA-Z0-9]+$/.test(value)) {
          message = '아이디는 영문과 숫자 조합만 가능합니다.';
        }
        break;
      case 'password':
        if (!value) {
          message = '비밀번호를 입력해주세요.';
        } else if (value.length < 8 || value.length > 20) {
          message = '비밀번호는 8~20자 이내로 입력해야 합니다.';
        }
        break;
      default:
        break;
    }
    return message;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setErrors((prevErrors) => ({
      ...prevErrors,
      [name]: validateField(name, value),
      submit: '',
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    let formHasErrors = false;
    const newErrors = {};
    for (const key in formData) {
      const message = validateField(key, formData[key]);
      if (message) {
        newErrors[key] = message;
        formHasErrors = true;
      }
    }

    if (formHasErrors) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      const res = await loginUser({
        loginId: formData.loginId,
        password: formData.password,
      });

      if (res.data && res.data.accessToken) {
        // ✅ Axios 기본 헤더 설정
        axios.defaults.headers.common['Authorization'] = `Bearer ${res.data.accessToken}`;

        // ✅ Zustand로 토큰 저장
        login(res.data.accessToken, res.data.userName);

        // ✅ Redux로 사용자 정보 요청
        dispatch(fetchIdentityInfo());

        // ✅ Snackbar로 로그인 완료 메시지
        navigate('/', {
          state: {
            snackbar: {
              open: true,
              message: '로그인 완료!',
              severity: 'success',
            },
          },
        });
      }
    } catch (err) {
      console.error(err);
      setErrors((prevErrors) => ({
        ...prevErrors,
        submit: '아이디 또는 비밀번호가 일치하지 않습니다.',
      }));
      setSnackbar({
        open: true,
        message: '아이디 또는 비밀번호가 일치하지 않습니다.',
        severity: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  const isFormValid = useMemo(() => {
    return (
      !errors.loginId && !errors.password && formData.loginId !== '' && formData.password !== ''
    );
  }, [errors.loginId, errors.password, formData.loginId, formData.password]);

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ mt: 4 }}>
      <TextField
        label="아이디"
        name="loginId"
        value={formData.loginId}
        onChange={handleChange}
        fullWidth
        margin="normal"
        required
        error={!!errors.loginId}
        helperText={errors.loginId || '아이디를 입력해주세요.'}
        FormHelperTextProps={{
          style: { color: errors.loginId ? 'red' : 'grey' },
        }}
      />
      <TextField
        label="비밀번호"
        type="password"
        name="password"
        value={formData.password}
        onChange={handleChange}
        fullWidth
        margin="normal"
        required
        error={!!errors.password}
        helperText={errors.password || '비밀번호를 입력해주세요.'}
        FormHelperTextProps={{
          style: { color: errors.password ? 'red' : 'grey' },
        }}
      />
      <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={loading || !isFormValid}>
        {loading ? '로그인 중...' : '로그인'}
      </Button>
      {errors.submit && (
        <Box sx={{ mt: 2, color: 'red' }}>
          <Typography>{errors.submit}</Typography>
        </Box>
      )}
      <FormSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      />
    </Box>
  );
};

export default LoginForm;
