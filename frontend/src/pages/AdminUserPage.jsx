import React, { useEffect, useState } from 'react';
import axios from 'axios';
import {
  Box,
  Button,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import FormSnackbar from '../components/FormSnackbar'; // 추가

const AdminUserPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // snackbar 상태 추가
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info',
  });

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('http://localhost:8080/api/admin/users');
      console.log("유저 목록 응답:", response.data);
      setUsers(response.data.content || []);
    } catch (err) {
      console.error('유저 목록 불러오기 실패:', err);
      setError('유저 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUserDetail = async (userId) => {
    setDetailLoading(true);
    try {
      const response = await axios.get(`http://localhost:8080/api/admin/users/${userId}`);
      setSelectedUser(response.data);
    } catch (err) {
      console.error('유저 상세정보 불러오기 실패:', err);
      setSnackbar({
        open: true,
        message: '유저 상세정보를 불러오지 못했습니다.',
        severity: 'error',
      });
    } finally {
      setDetailLoading(false);
    }
  };

  const handleViewUser = (userId) => {
    fetchUserDetail(userId);
  };

  const handleBanUser = async (userId) => {
    if (!window.confirm('정말로 해당 유저를 Ban 처리하시겠습니까?')) return;
    try {
      await axios.put(`http://localhost:8080/api/admin/users/${userId}/status`, {
        status: 'BANNED',
      });
      setSnackbar({
        open: true,
        message: '유저가 Ban 처리되었습니다.',
        severity: 'success',
      });
      fetchUsers();
    } catch (err) {
      console.error('유저 Ban 처리 실패:', err);
      setSnackbar({
        open: true,
        message: '유저 Ban 처리에 실패했습니다.',
        severity: 'error',
      });
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        관리자 - 유저 관리
      </Typography>

      {loading ? (
        <Box sx={{ textAlign: 'center', mt: 5 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Typography color="error">{error}</Typography>
      ) : (
        <Paper>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>로그인 ID</TableCell>
                <TableCell>이름</TableCell>
                <TableCell>전화번호</TableCell>
                <TableCell>관리자 여부</TableCell>
                <TableCell>차단 여부</TableCell>
                <TableCell>가입일</TableCell>
                <TableCell>지역</TableCell>
                <TableCell>액션</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.length > 0 ? (
                users.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>{user.id}</TableCell>
                    <TableCell>{user.loginId}</TableCell>
                    <TableCell>{user.userName}</TableCell>
                    <TableCell>{user.phone || '없음'}</TableCell>
                    <TableCell>{user.isAdmin ? 'O' : 'X'}</TableCell>
                    <TableCell>{user.isBanned ? '차단됨' : '정상'}</TableCell>
                    <TableCell>{new Date(user.createdAt).toLocaleString()}</TableCell>
                    <TableCell>{user.area?.areaName || '미지정'}</TableCell>
                    <TableCell>
                      <Button size="small" onClick={() => handleViewUser(user.id)}>
                        보기
                      </Button>
                      {!user.isBanned && !user.isAdmin && (
                        <Button
                          size="small"
                          color="error"
                          onClick={() => handleBanUser(user.id)}
                          sx={{ ml: 1 }}
                        >
                          Ban
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={9} align="center">
                    등록된 유저가 없습니다.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Paper>
      )}

      {/* 유저 상세 다이얼로그 */}
      <Dialog
        open={!!selectedUser}
        onClose={() => setSelectedUser(null)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>유저 상세정보</DialogTitle>
        <DialogContent dividers>
          {detailLoading ? (
            <Box sx={{ textAlign: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : selectedUser ? (
            <>
              <Typography><strong>ID:</strong> {selectedUser.id}</Typography>
              <Typography><strong>로그인 ID:</strong> {selectedUser.loginId}</Typography>
              <Typography><strong>이름:</strong> {selectedUser.userName}</Typography>
              <Typography><strong>전화번호:</strong> {selectedUser.phone || '없음'}</Typography>
              <Typography><strong>관리자 여부:</strong> {selectedUser.isAdmin ? 'O' : 'X'}</Typography>
              <Typography><strong>차단 여부:</strong> {selectedUser.isBanned ? '차단됨' : '정상'}</Typography>
              <Typography><strong>가입일:</strong> {new Date(selectedUser.createdAt).toLocaleString()}</Typography>
              <Typography><strong>지역:</strong> {selectedUser.area?.areaName || '미지정'}</Typography>
              {selectedUser.bannedAt && (
                <Typography><strong>차단 일시:</strong> {new Date(selectedUser.bannedAt).toLocaleString()}</Typography>
              )}
              {selectedUser.banReason && (
                <Typography><strong>차단 사유:</strong> {selectedUser.banReason}</Typography>
              )}
            </>
          ) : null}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSelectedUser(null)}>닫기</Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar 알림 */}
      <FormSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      />
    </Box>
  );
};

export default AdminUserPage;