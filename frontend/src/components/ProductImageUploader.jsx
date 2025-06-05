import React, { useRef } from "react";
import { Box, Paper, Typography, IconButton } from "@mui/material";
import AddPhotoAlternateIcon from "@mui/icons-material/AddPhotoAlternate";
import CloseIcon from "@mui/icons-material/Close";

const MAX_IMAGES = 10;

/**
 * ProductImageUploader
 * @param {Object} props
 * @param {Array} props.images - [{file, url, imageId?}]
 * @param {Function} props.onChange - (newImages) => void
 */
const ProductImageUploader = ({ images, onChange }) => {
  const fileInputRef = useRef();

  const handleImageSelect = (e) => {
    const files = Array.from(e.target.files);
    if (images.length + files.length > MAX_IMAGES) {
      alert(`이미지는 최대 ${MAX_IMAGES}장까지 등록할 수 있습니다.`);
      return;
    }
    const newImages = files.map((file) => ({
      file,
      url: URL.createObjectURL(file),
    }));
    onChange([...images, ...newImages]);
  };

  const handleRemoveImage = (idx) => {
    onChange(images.filter((_, i) => i !== idx));
  };

  return (
    <Box display="flex" alignItems="flex-start">
      {/* 상품이미지 텍스트 */}
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          minWidth: 120,
          width: 120,
          height: 200,
          mr: 2,
          flexShrink: 0,
        }}
      >
        <Typography fontWeight={700} fontSize={18}>
          상품이미지 {" "}
          <Typography component="span" color="#888" fontWeight={400} display="inline">
            ({images.length}/{MAX_IMAGES})
          </Typography>
        </Typography>
      </Box>
      {/* 이미지 등록 버튼 + 미리보기 이미지들 */}
      <Box sx={{ flex: 1, display: "flex", flexWrap: "wrap", gap: 2, alignItems: "flex-start" }}>
        <Paper
          variant="outlined"
          sx={{
            width: 200,
            height: 200,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            flexDirection: "column",
            cursor: "pointer",
            position: "relative",
            minWidth: 200,
            minHeight: 200,
            bgcolor: "#fafafa",
            borderRadius: 0,
          }}
          onClick={() => fileInputRef.current.click()}
        >
          <input
            type="file"
            accept="image/*"
            multiple
            ref={fileInputRef}
            style={{ display: "none" }}
            onChange={handleImageSelect}
          />
          <AddPhotoAlternateIcon sx={{ fontSize: 48, color: "#bbb" }} />
          <Typography mt={1.5} fontSize={18} color="#888">
            이미지 등록
          </Typography>
        </Paper>
        {images.map((img, idx) => (
          <Paper
            key={idx}
            variant="outlined"
            sx={{
              width: 200,
              height: 200,
              minWidth: 200,
              minHeight: 200,
              position: "relative",
              overflow: "hidden",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              bgcolor: "#fafafa",
              borderRadius: 0,
            }}
          >
            {idx === 0 && (
              <Box
                sx={{
                  position: "absolute",
                  top: 8,
                  left: 8,
                  background: "rgba(0,0,0,0.6)",
                  color: "#fff",
                  fontSize: 13,
                  px: 1.5,
                  py: 0.5,
                  borderRadius: 2,
                  zIndex: 2,
                }}
              >
                대표이미지
              </Box>
            )}
            <Box
              component="img"
              src={img.url}
              alt={`상품이미지${idx + 1}`}
              sx={{
                width: "100%",
                height: "100%",
                objectFit: "cover",
              }}
            />
            <IconButton
              size="small"
              onClick={() => handleRemoveImage(idx)}
              sx={{
                position: "absolute",
                top: 8,
                right: 8,
                background: "rgba(0,0,0,0.6)",
                color: "#fff",
                "&:hover": { background: "rgba(0,0,0,0.8)" },
                zIndex: 2,
              }}
            >
              <CloseIcon fontSize="medium" />
            </IconButton>
          </Paper>
        ))}
      </Box>
    </Box>
  );
};

export default ProductImageUploader;
