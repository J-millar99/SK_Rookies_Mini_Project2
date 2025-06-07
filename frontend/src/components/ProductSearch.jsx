import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import AreaSelectModal from './AreaSelectModal';
import KeywordSearch from './KeywordSearch';
import CategorySelect from './CategorySelect';
import { getCategories } from '../api/category';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PriceToggleButton from './PriceToggleButton';
import SearchIcon from '@mui/icons-material/Search';
import { Box, Button, IconButton, Paper, useTheme } from '@mui/material';
import FormSnackbar from './FormSnackbar'; // 추가

const ProductSearch = () => {
  const [modalOpen, setModalOpen] = useState(false);
  const [selected, setSelected] = useState(null);
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [showPriceInputs, setShowPriceInputs] = useState(false);
  const [categories, setCategories] = useState([]);

  // snackbar 상태 추가
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'error',
  });

  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();

  React.useEffect(() => {
    const params = new URLSearchParams(location.search);
    setKeyword(params.get('keyword') || '');
    setCategory(params.get('category') || '');
    setMinPrice(params.get('minPrice') || '');
    setMaxPrice(params.get('maxPrice') || '');
  }, [location.search]);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const responseData = await getCategories();
        if (responseData && Array.isArray(responseData.data)) {
          setCategories([{ categoryName: '전체', categoryId: '' }, ...responseData.data]);
        } else {
          console.error('Unexpected categories data format:', responseData);
          setCategories([{ categoryName: '전체', categoryId: '' }]);
        }
      } catch (error) {
        console.error('카테고리 목록을 불러오는데 실패했습니다.', error);
        setCategories([{ categoryName: '전체', categoryId: '' }]);
      }
    };
    fetchCategories();
  }, []);

  const handleSearch = () => {
    const params = new URLSearchParams();
    params.set('page', 0);
    params.set('size', 10);
    params.set('sort', 'createdAt,desc');
    if (selected && selected.areaName) {
      params.set('area', selected.areaName);
    }
    if (keyword) params.set('keyword', keyword);
    if (category) params.set('category', category);
    if (minPrice) params.set('minPrice', minPrice);
    if (maxPrice) params.set('maxPrice', maxPrice);
    if (
      minPrice &&
      maxPrice &&
      Number(minPrice.replace(/,/g, "")) > Number(maxPrice.replace(/,/g, ""))
    ) {
      setSnackbar({
        open: true,
        message: '최소 가격은 최대 가격보다 작거나 같아야 합니다.',
        severity: 'error',
      });
      return; // 페이지 이동 막음
    }
    navigate(`/products?${params.toString()}`);
  };

  return (
    <Box
      sx={{
        display: 'flex',
        gap: 1.5,
        alignItems: 'flex-start',
        mt: 1.25,
        mr: 4,
        ml: 6.2,
        position: 'relative',
      }}
    >
      {/* 카테고리 입력 */}
      <CategorySelect
        value={category}
        onChange={(e) => setCategory(e.target.value)}
        options={categories.map((cat) => ({ value: cat.categoryName, label: cat.categoryName }))}
      />
      {/* 지역 선택 버튼 */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, mt: 0.25 }}>
        <Button
          onClick={() => setModalOpen(true)}
          variant="outlined"
          color="primary"
          sx={{
            borderRadius: 25,
            background: '#fff',
            color: '#222',
            fontWeight: 700,
            textTransform: 'none',
            borderColor: '#eee',
            fontSize: 16,
            display: 'flex',
            alignItems: 'center',
            '&:hover': {
              borderColor: theme.palette.error.main,
              background: theme.palette.background.default,
            },
          }}
        >
          <LocationOnIcon sx={{ color: theme.palette.error.main, fontSize: 26, ml: -1 }} />
          {selected && selected.areaName ? selected.areaName : '지역 선택'}
        </Button>
      </Box>
      {/* 검색 입력 영역 */}
      <Paper
        elevation={1}
        sx={{
          display: 'flex',
          alignItems: 'center',
          minWidth: 550,
          borderRadius: 25,
          px: 0.2,
          py: 0.5,
          mr: 4,
          boxShadow: '0 1px 4px rgba(0,0,0,0.07)',
          position: 'relative',
          bgcolor: '#fff',
          height: 42,
        }}
      >
        {/* ₩ 버튼 (왼쪽 끝) */}
        <PriceToggleButton
          showPriceInputs={showPriceInputs}
          setShowPriceInputs={setShowPriceInputs}
          minPrice={minPrice}
          setMinPrice={setMinPrice}
          maxPrice={maxPrice}
          setMaxPrice={setMaxPrice}
        />
        {/* 키워드 입력창 (가운데) */}
        <KeywordSearch
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') handleSearch();
          }}
          style={{
            zIndex: 1,
            background: 'transparent',
            height: 32,
            fontSize: 16,
          }}
        />
        {/* 검색 버튼 (오른쪽 끝) */}
        <IconButton
          onClick={handleSearch}
          sx={{
            height: 40,
            width: 40,
            mt: 0.05,
            bgcolor: theme.palette.error.main,
            color: '#fff',
            border: 'none',
            borderRadius: 25,
            '&:hover': { bgcolor: theme.palette.error.dark },
          }}
          aria-label="검색"
        >
          <SearchIcon />
        </IconButton>
      </Paper>
      {/* 지역 선택 모달 */}
      {modalOpen && (
        <AreaSelectModal
          onSelect={(area) => {
            setSelected(area);
            setModalOpen(false);
          }}
          onClose={() => setModalOpen(false)}
          onReset={() => {
            setSelected(null);
            setModalOpen(false);
          }}
        />
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

export default ProductSearch;