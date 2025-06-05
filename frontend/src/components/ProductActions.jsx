import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom"; // 추가
import { Button } from "@mui/material";

const buttonStyle = {
  width: 200,
  height: 60,
  padding: '8px 20px',
  borderRadius: 0,
  cursor: 'pointer',
  fontWeight: 700,
};

const ProductActions = ({ productId, isInitiallyLiked = false }) => {
  const [isLiked, setIsLiked] = useState(isInitiallyLiked);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleWishlist = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        alert("먼저 로그인을 진행해주세요!");
        setLoading(false);
        return;
      }
      const res = await axios.put(
        `/api/wishlist/${productId}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (res.data?.success) {
        setIsLiked(res.data.data.isLiked);
        alert("찜 목록에 추가되었습니다.");
      } else {
          alert("찜 목록에서 제거되었습니다.");
      }
    } catch (e) {
      if (e.response && e.response.status === 404) {
        navigate("/err/NotFound");
      } else {
        alert("찜하기 처리 중 오류가 발생했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        position: 'absolute',
        left: 0,
        bottom: 0,
        width: '100%',
        display: 'flex',
        justifyContent: 'flex-end',
        gap: 12,
        paddingRight: 40,
        boxSizing: 'border-box',
        fontSize: 22,
        fontWeight: 700,
      }}
    >
      <Button
        variant={isLiked ? "contained" : "outlined"}
        color={isLiked ? "error" : "inherit"}
        sx={{
          ...buttonStyle,
          border: isLiked ? '1px solid #EA002C' : '1px solid #e0e0e0',
          background: isLiked ? '#EA002C' : '#fff',
          color: isLiked ? '#fff' : '#222',
          opacity: loading ? 0.6 : 1,
          fontSize: 22,
          fontWeight: 700,
        }}
        onClick={handleWishlist}
        disabled={loading}
      >
        {isLiked ? "찜 취소" : "찜하기"}
      </Button>
      <Button
        variant="contained"
        color="primary"
        sx={{ ...buttonStyle, fontSize: 22, fontWeight: 700 }}
      >
        대화하기
      </Button>
      <Button
        variant="contained"
        color="success"
        sx={{ ...buttonStyle, fontSize: 22, fontWeight: 700 }}
      >
        바로구매
      </Button>
    </div>
  );
};

export default ProductActions;