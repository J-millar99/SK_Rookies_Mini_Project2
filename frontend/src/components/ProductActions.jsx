import React, { useState, useEffect } from "react";
import { Button } from "@mui/material";
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import FavoriteIcon from '@mui/icons-material/Favorite';
import axios from "axios";
import FormSnackbar from "./FormSnackbar";
import reservationService from "../api/reservationService";

const buttonStyle = {
  width: 200,
  height: 60,
  padding: '8px 20px',
  borderRadius: 0,
  cursor: 'pointer',
  fontSize: 22,
  fontWeight: 700,
};

const ProductActions = ({
  productId,
  isOwner = false,
  onEdit,
  onDelete,
  isInitiallyLiked = false,
}) => {
  const [isLiked, setIsLiked] = useState(isInitiallyLiked);
  const [loading, setLoading] = useState(false);
  const [iconBump, setIconBump] = useState(false);

  // isInitiallyLiked가 바뀌면 isLiked도 동기화
  useEffect(() => {
    setIsLiked(isInitiallyLiked);
  }, [isInitiallyLiked]);

  // 스낵바 상태
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "info",
  });

  const handleSnackbarClose = () => setSnackbar({ ...snackbar, open: false });

  // 거래 신청 버튼 클릭 핸들러
  const handleReservation = async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      setSnackbar({
        open: true,
        message: "먼저 로그인을 진행해주세요!",
        severity: "error",
      });
      return;
    }
    try {
      const result = await reservationService.createReservation(productId);
      console.log("거래 신청 응답:", result);
      if (result && result.success) {
        setSnackbar({
          open: true,
          message: "거래 신청이 정상적으로 되었습니다!",
          severity: "success",
        });
      } else if (result && result.error && result.error.statusCode === 400 && result.error.message) {
        setSnackbar({
          open: true,
          message: result.error.message,
          severity: "error",
        });
      } else {
        setSnackbar({
          open: true,
          message: "거래 신청 중 오류가 발생했습니다.",
          severity: "error",
        });
      }
    } catch {
      setSnackbar({
        open: true,
        message: "거래 신청 중 오류가 발생했습니다.",
        severity: "error",
      });
    }
  };

  // 찜하기 버튼 클릭 핸들러
  const handleWishlist = async () => {
    setLoading(true);
    setIconBump(true);
    setTimeout(() => setIconBump(false), 250);
    try {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        setSnackbar({
          open: true,
          message: "먼저 로그인을 진행해주세요!",
          severity: "error",
        });
        setLoading(false);
        return;
      }
      // DibsController에 맞춰 PUT 요청
      const res = await axios.put(
        `http://localhost:8080/api/dibs/${productId}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      console.log("찜하기 응답:", res.data);
      if (res.data?.success) {
        setIsLiked(res.data.data.liked); // liked 값으로 상태 변경
        setSnackbar({
          open: true,
          message: res.data.message || (res.data.data.liked ? "찜 목록에 추가되었습니다." : "찜 목록에서 제거되었습니다."),
          severity: res.data.data.liked ? "success" : "info",
        });
      } else {
        setSnackbar({
          open: true,
          message: "찜하기 처리 중 오류가 발생했습니다.",
          severity: "error",
        });
      }
    } catch (e) {
      if (e.response && e.response.status === 404) {
        setSnackbar({
          open: true,
          message: "ID를 찾을 수 없습니다!",
          severity: "error",
        });
      } else {
        setSnackbar({
          open: true,
          message: "찜하기 처리 중 오류가 발생했습니다.",
          severity: "error",
        });
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
      }}
    >
      {isOwner ? (
        <>
          <Button
            variant="contained"
            color="info"
            sx={{
              ...buttonStyle,
            }}
            onClick={onEdit}
          >
            상품 수정
          </Button>
          <Button
            variant="contained"
            color="error"
            sx={{
              ...buttonStyle,
            }}
            onClick={onDelete}
          >
            상품 삭제
          </Button>
        </>
      ) : (
        <>
          <Button
            variant={isLiked ? "contained" : "outlined"}
            color={isLiked ? "error" : "inherit"}
            sx={{
              ...buttonStyle,
              border: '1.5px solid #e0e0e0',
              background: '#fff',
              color: '#222',
              opacity: loading ? 0.6 : 1,
              fontSize: 22,
              fontWeight: 700,
            }}
            onClick={handleWishlist}
            disabled={loading}
            endIcon={
              <span
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  transition: 'transform 0.25s cubic-bezier(.4,2,.6,1)',
                  transform: iconBump ? 'scale(1.4)' : 'scale(1)',
                }}
              >
                {isLiked
                  ? <FavoriteIcon color="error" sx={{ fontSize: '36px' }} />
                  : <FavoriteBorderIcon color="default" sx={{ fontSize: '36px' }} />
                }
              </span>
            }
          >
            {isLiked ? "찜 취소" : "찜하기"}
          </Button>
          <Button
            variant="contained"
            color="success"
            sx={{ ...buttonStyle, fontSize: 22, fontWeight: 700 }}
            onClick={handleReservation}
          >
            거래 신청
          </Button>
        </>
      )}
      <FormSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={handleSnackbarClose}
      />
    </div>
  );
};

export default ProductActions;