import { useParams } from "react-router-dom";
import { products } from "../constants/ExpProductDB";
import { Box, Typography, Divider, Grid, IconButton } from "@mui/material";
import NotFound from "../err/NotFound";
import { FormatTime } from "../utils/FormatTime";
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import React, { useState } from "react";
import ProductsList from "../components/ProductsList";

const ProductDetailPage = () => {
  const { product_id } = useParams();
  const product = products.find(p => String(p.id) === String(product_id));
  const [imgIdx, setImgIdx] = useState(0);

  if (!product) {
    return <NotFound />;
  }

  const images = product.images || [];
  const hasImages = images.length > 0;

  const handlePrev = (e) => {
    e.stopPropagation();
    setImgIdx(idx => idx > 0 ? idx - 1 : idx);
  };

  const handleNext = (e) => {
    e.stopPropagation();
    setImgIdx(idx => idx < images.length - 1 ? idx + 1 : idx);
  };

  // 비슷한 상품 리스트 (같은 카테고리, 자기 자신 제외)
  const similarProducts = products
    .filter(
      p =>
        p.category === product.category &&
        p.id !== product.id
    )
    .slice(0, 5);

  return (
    <Box sx={{ px: 5, py: 4 }}>
      <Grid container spacing={4}>
        {/* 이미지와 텍스트를 한 줄로 묶는 Box */}
        <Grid item xs={12}>
          <Box
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', md: 'row' },
              alignItems: 'flex-start',
              gap: 4,
              minHeight: 400,
              position: 'relative'
            }}
          >
            {/* 왼쪽: 이미지 영역 */}
            <Box
              sx={{
                width: 400,
                aspectRatio: '1/1',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                bgcolor: '#fafafa',
                borderRadius: 2,
                overflow: 'hidden',
                position: 'relative',
                flexShrink: 0,
              }}
            >
              {hasImages ? (
                <>
                  <img
                    src={images[imgIdx]}
                    alt={product.title}
                    style={{ width: '100%', height: 'auto', objectFit: 'contain', maxHeight: 400 }}
                  />
                  {images.length > 1 && (
                    <>
                      <IconButton
                        onClick={handlePrev}
                        disabled={imgIdx === 0}
                        sx={{
                          position: 'absolute',
                          left: 8,
                          top: '50%',
                          transform: 'translateY(-50%)',
                          bgcolor: 'rgba(255,255,255,0.7)',
                          '&:hover': { bgcolor: 'rgba(255,255,255,0.9)' }
                        }}
                        size="small"
                      >
                        <ArrowBackIosNewIcon />
                      </IconButton>
                      <IconButton
                        onClick={handleNext}
                        disabled={imgIdx === images.length - 1}
                        sx={{
                          position: 'absolute',
                          right: 8,
                          top: '50%',
                          transform: 'translateY(-50%)',
                          bgcolor: 'rgba(255,255,255,0.7)',
                          '&:hover': { bgcolor: 'rgba(255,255,255,0.9)' }
                        }}
                        size="small"
                      >
                        <ArrowForwardIosIcon />
                      </IconButton>
                      {/* 이미지 인덱스 표시 */}
                      <Box
                        sx={{
                          position: 'absolute',
                          bottom: 8,
                          left: '50%',
                          transform: 'translateX(-50%)',
                          bgcolor: 'rgba(0,0,0,0.5)',
                          color: '#fff',
                          borderRadius: 2,
                          px: 1.5,
                          fontSize: 13,
                          py: 0.2,
                        }}
                      >
                        {imgIdx + 1} / {images.length}
                      </Box>
                    </>
                  )}
                </>
              ) : (
                <Typography color="text.secondary">이미지 없음</Typography>
              )}
            </Box>
            {/* 오른쪽: 상세 정보 */}
            <Box sx={{ flex: 1, textAlign: 'left', display: 'flex', flexDirection: 'column' }}>
              <Typography variant="h4" sx={{ mb: 2 }}>{product.title}</Typography>
              <Typography variant="subtitle1" sx={{ mb: 1, color: '#777', display: 'flex', alignItems: 'center', gap: 2 }}>
                카테고리: {product.category}
                <Divider orientation="vertical" flexItem sx={{ mx: 2 }} />
                상태: {product.status}
                <Divider orientation="vertical" flexItem sx={{ mx: 2 }} />
                등록일: {FormatTime(product.createdAt)}
              </Typography>
              <Divider sx={{ mb: 2, width: 725 }} />
              <Typography variant="h3" sx={{ mb: 2, fontWeight: 700 }}>
                {product.price.toLocaleString()}원
              </Typography>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>
                지역: {product.area || "지역정보 없음"}
              </Typography>
            </Box>
            {/* 버튼 영역: 이미지+정보를 감싸는 Box의 맨 아래에 절대배치 */}
            <Box
              sx={{
                position: 'absolute',
                left: 0,
                bottom: 0,
                width: '100%',
                display: 'flex',
                justifyContent: { xs: 'flex-start', md: 'flex-end' },
                gap: 3,
                pr: 5,
                boxSizing: 'border-box',
                fontSize: 22,
                fontWeight: 700,
              }}
            >
              <button style={{
                width: 200,
                height: 60,
                padding: '8px 20px',
                border: '1px solid #e0e0e0',
                background: '#fff',
                cursor: 'pointer',
                borderRadius: 0,
              }}>
                찜하기
              </button>
              <button style={{
                width: 200,
                height: 60,
                padding: '8px 20px',
                border: '1px solid #1976d2',
                background: '#1976d2',
                color: '#fff',
                cursor: 'pointer',
                borderRadius: 0,
              }}>
                대화하기
              </button>
              <button style={{
                width: 200,
                height: 60,
                padding: '8px 20px',
                border: '1px solid #43a047',
                background: '#43a047',
                color: '#fff',
                cursor: 'pointer',
                borderRadius: 0,
              }}>
                바로구매
              </button>
            </Box>
          </Box>
        </Grid>
        {/* 설명은 아래에 별도 Row로 */}
        <Grid item xs={12}>
          {/* 비슷한 상품 추천 영역 */}
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, textAlign: 'left' }}>
              이 상품과 비슷해요
            </Typography>
            <ProductsList
              products={similarProducts}
              onProductClick={id => window.location.href = `/products/${id}`}
              formatTime={FormatTime}
            />
          </Box>
          <Box>
            <Divider sx={{ mb: 2, width: 1120 , bgcolor: 'black' }} />
            <Box sx={{ display: 'flex', gap: 4, alignItems: 'flex-start' }}>
              {/* 상품정보 */}
              <Box sx={{ flex: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                  상품정보
                </Typography>
                <Divider sx={{ mb: 2, width: 800 }} />
                <Typography variant="body1" sx={{ mt: 2, whiteSpace: 'pre-line' }}>
                  {product.description}
                </Typography>
              </Box>
              {/* 판매자 정보 (추가 미결정) */}
              <Box sx={{
                flex: 1,
                minWidth: 280,
                maxWidth: 340,
                bgcolor: '#fff',
                borderRadius: 2,
                boxShadow: 1,
                p: 3,
                border: '1px solid #eee'
              }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                  판매자 정보
                </Typography>
                {/* 판매자 정보 예시 (실제 데이터에 맞게 수정) */}
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 2 }}>
                  <Box
                    component="img"
                    src={product.seller?.profileImage || '/default-profile.png'}
                    alt="판매자 프로필"
                    sx={{ width: 64, height: 64, borderRadius: '50%', mb: 1, objectFit: 'cover' }}
                  />
                  <Typography sx={{ fontWeight: 600 }}>
                    {product.seller?.name || '판매자명'}
                  </Typography>
                  <Typography sx={{ color: '#888', fontSize: 14 }}>
                    평균 평점 | -
                  </Typography>
                  <button
                    style={{
                      marginTop: 50,
                      padding: '6px 18px',
                      border: '1px solid #e57373',
                      borderRadius: 4,
                      background: '#EA002C',
                      color: 'white',
                      cursor: 'pointer',
                      fontWeight: 600
                    }}
                    onClick={() => alert('신고 처리가 완료되었습니다.')}
                  >
                    신고하기
                  </button>
                </Box>
              </Box>
            </Box>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ProductDetailPage;