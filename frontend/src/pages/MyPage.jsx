import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setIdentityInfo, clearAuthState } from '../features/auth/authSlice';
import useAuthStore from '../store/authStore';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const MyPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { identityInfo } = useSelector((state) => state.auth);
  const [name, setName] = useState(identityInfo?.name || '');
  const [phone, setPhone] = useState(identityInfo?.phone || '');
  const [region, setRegion] = useState(identityInfo?.region || '');
  const [editing, setEditing] = useState(false);
  const [password, setPassword] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const handleSave = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await axios.put(
        'http://localhost:8080/api/users/profile',
        { name, phone, region },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );
      dispatch(setIdentityInfo(response.data));
      setEditing(false);
      alert('프로필이 성공적으로 업데이트되었습니다.');
    } catch (error) {
      console.error('프로필 업데이트 실패:', error);
      alert('프로필 업데이트에 실패했습니다.');
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
        data: { password }
      });

      alert('계정이 삭제되었습니다.');

      dispatch(clearAuthState());
      useAuthStore.getState().logout();
      navigate('/login');
    } catch (error) {
      console.error('계정 삭제 실패:', error);
      alert('비밀번호가 일치하지 않거나 삭제에 실패했습니다.');
    }
  };

  return (
    <div>
      <h2>마이페이지</h2>
      {editing ? (
        <div>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="이름"
          />
          <input
            type="text"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="전화번호"
          />
          <input
            type="text"
            value={region}
            onChange={(e) => setRegion(e.target.value)}
            placeholder="지역"
          />
          <button onClick={handleSave}>저장</button>
          <button onClick={() => setEditing(false)}>취소</button>
        </div>
      ) : (
        <div>
          <p>이름: {identityInfo?.name}</p>
          <p>전화번호: {identityInfo?.phone}</p>
          <p>지역: {identityInfo?.region}</p>
          <button onClick={() => setEditing(true)}>수정</button>
        </div>
      )}

      <hr />

      {!showDeleteConfirm ? (
        <button onClick={() => setShowDeleteConfirm(true)} style={{ color: 'red' }}>
          계정 삭제
        </button>
      ) : (
        <div>
          <p>계정을 삭제하려면 비밀번호를 입력하세요:</p>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호"
          />
          <button onClick={handleDeleteAccount} style={{ color: 'red' }}>삭제 확인</button>
          <button onClick={() => setShowDeleteConfirm(false)}>취소</button>
        </div>
      )}
    </div>
  );
};

export default MyPage;
