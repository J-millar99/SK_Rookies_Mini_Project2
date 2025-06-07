import React, { useEffect } from 'react';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import { Box, Button, Divider, InputAdornment, TextField, Typography } from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import ProductImageUploader from '../components/ProductImageUploader';
import FormSnackbar from '../components/FormSnackbar';
import useProductForm from '../hooks/useProductForm';

const MAX_IMAGES = 3;

const ProductRegisterPage = ({ editMode }) => {
  const { id } = useParams();
  const isEdit = !!id || editMode;

  const navigate = useNavigate();
  const [userProfile, setUserProfile] = React.useState({ is_banned: 'false' });
  const [userProductCount, setUserProductCount] = React.useState(0);
  const [categoryOptions, setCategoryOptions] = React.useState([]);

  const {
    form,
    setForm,
    formError,
    setFormError,
    openError,
    setOpenError,
    loading,
    setLoading,
    handleChange,
    handlePriceChange,
    validate,
    showError,
  } = useProductForm({ isEdit, id, userProfile, userProductCount, navigate });

  // accessToken에서 userId 추출
  const token = localStorage.getItem('accessToken');
  let userId = '';
  if (token) {
    try {
      const payload = jwtDecode(token);
      userId = payload.sub; // JWT의 sub가 userId
    } catch {
      userId = '';
    }
  }

  // 카테고리 목록 불러오기
  useEffect(() => {
    axios.get('http://localhost:8080/categories')
      .then(res => {
        const arr = Array.isArray(res.data.data) ? res.data.data : [];
        setCategoryOptions(
          arr.map(cat => ({
            value: String(cat.categoryId),
            label: cat.categoryName
          }))
        );
      })
      .catch(() => setCategoryOptions([]));
  }, []);

  // 상품 정보와 이미지 한 번에 불러오기
  useEffect(() => {
    if (isEdit && !id) {
      setFormError("ID를 찾을 수 없습니다!");
      setOpenError(true);
      return;
    }
    if (isEdit && id) {
      setLoading(true);
      axios
        .get(`http://localhost:8080/api/users/products/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })
        .then(async (res) => {
          const data = res.data;
          let categoryValue = '';
          if (data.categoryName) {
            const found = categoryOptions.find((opt) => opt.label === data.categoryName);
            categoryValue = found ? String(found.value) : '';
          }
          // 이미지도 같이 불러오기
          let imgArr = [];
          try {
            const imgRes = await axios.get(`http://localhost:8080/images/product/${id}`);
            imgArr = Array.isArray(imgRes.data)
              ? imgRes.data.map(img => ({
                  file: null,
                  url: img.imageUrl.startsWith('http')
                    ? img.imageUrl.replace('http://localhost:3000', 'http://localhost:8080')
                    : `http://localhost:8080${img.imageUrl}`,
                  imageId: img.imageId,
                }))
              : [];
          } catch {
            imgArr = [];
          }
          setForm({
            title: data.title || '',
            content: data.content || '',
            price: data.price ? data.price.toLocaleString() : '',
            category: categoryValue,
            images: imgArr,
          });
        })
        .catch(() => {
          setFormError('상품 정보를 불러올 수 없습니다.');
          setOpenError(true);
          navigate('/my-products');
        })
        .finally(() => setLoading(false));
    }
  }, [isEdit, id, token, navigate, categoryOptions, setForm, setLoading, setFormError, setOpenError]);

  // 사용자 상태 정보 불러오기
  useEffect(() => {
    window.scrollTo(0, 0);
    if (userId) {
      axios
        .get(`http://localhost:8080/api/users/profile`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })
        .then((res) => {
          setUserProfile({
            is_banned: String(res.data.is_banned),
          });
        })
        .catch((err) => console.error('사용자 상태 정보 불러오기 실패', err));
    }
  }, [userId, token]);

  // 사용자 상품 개수 불러오기
  useEffect(() => {
    if (userId) {
      axios
        .get(`http://localhost:8080/api/users/products`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })
        .then((res) => {
          setUserProductCount(Array.isArray(res.data.content) ? res.data.content.length : 0);
        })
        .catch((err) => console.error('사용자의 상품 개수 불러오기 실패', err));
    }
  }, [userId, token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setOpenError(false);
    if (!validate()) return;
    setLoading(true);
    try {
      if (isEdit) {
        const product_id = id;
        const updateData = {
          title: form.title,
          content: form.content,
          price: parseInt(form.price.replace(/,/g, ''), 10),
          categoryId: parseInt(form.category, 10),
        };
        await axios.put(`http://localhost:8080/api/users/products/${product_id}`, updateData, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });
        // 이미지가 있으면 별도 업로드
        if (form.images && form.images.length > 0 && form.images.some(img => img.file)) {
          const imageFormData = new FormData();
          imageFormData.append('productId', product_id);
          form.images.forEach((img) => {
            if (img.file) imageFormData.append('images', img.file);
          });
          await axios.post('http://localhost:8080/images', imageFormData, {
            headers: {
              Authorization: `Bearer ${token}`,
              'Content-Type': 'multipart/form-data',
            },
          });
        }
        setFormError('상품이 수정되었습니다.');
        setOpenError(true);
        navigate(`/my-products`, {
          state: {
            snackbar: {
              open: true,
              message: '상품이 수정되었습니다.',
              severity: 'success',
            },
          },
        });
      } else {
        const formData = new FormData();
        formData.append('title', form.title);
        formData.append('content', form.content);
        formData.append('price', parseInt(form.price.replace(/,/g, ''), 10));
        formData.append('categoryId', String(form.category));
        const res = await axios.post(`http://localhost:8080/api/users/products`, formData, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data',
          },
        });
        // 상품 등록 성공 후 이미지가 있으면 별도 업로드
        if (res.data && res.data.id) {
          if (form.images && form.images.length > 0 && form.images.some(img => img.file)) {
            const imageFormData = new FormData();
            imageFormData.append('productId', res.data.id);
            form.images.forEach((img) => {
              if (img.file) imageFormData.append('images', img.file);
            });
            for (let pair of imageFormData.entries()) {
              console.log(pair[0], pair[1]);
            }
            await axios.post('http://localhost:8080/images', imageFormData, {
              headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'multipart/form-data',
              },
            });
          }
          // snackbar 메시지와 함께 바로 이동
          navigate('/my-products', {
            state: {
              snackbar: {
                open: true,
                message: '성공적으로 등록되었습니다.',
                severity: 'success',
              },
            },
          });
        }
      }
    } catch (e) {
      if (e.response && e.response.status === 422) {
        showError('비즈니스 규칙 위반!');
      } else {
        showError(
          isEdit ? '상품 수정 중 오류가 발생했습니다.' : '상품 등록 중 오류가 발생했습니다.'
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ width: 1100, mx: 'auto', p: 3 }}>
      <Typography variant="h4" fontWeight={700} mb={4} ml={4} align="left">
        {isEdit ? '상품 수정' : '상품 등록'}
      </Typography>
      <Divider sx={{ mb: 4, borderColor: '#222', borderWidth: 2 }} />

      {/* 상품이미지 */}
      <Box sx={{ mb: 4 }}>
        <ProductImageUploader
          images={form.images}
          onChange={(imgs) => setForm((prev) => ({ ...prev, images: imgs }))}
        />
      </Box>
      <Divider sx={{ my: 3, borderColor: '#eee', borderWidth: 1.5 }} />

      {/* 상품명 */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Typography fontWeight={700} sx={{ minWidth: 100 }}>
          상품명
        </Typography>
        <TextField
          name="title"
          value={form.title}
          onChange={handleChange}
          required
          fullWidth
          placeholder="상품명을 입력해 주세요."
          inputProps={{ maxLength: 50 }}
          sx={{ flex: 1 }}
        />
        <Typography variant="body2" color="#888" sx={{ ml: 1 }}>
          {form.title.length}/50
        </Typography>
      </Box>
      <Divider sx={{ my: 3, borderColor: '#eee', borderWidth: 1.5 }} />

      {/* 카테고리 */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'flex-start', gap: 2 }}>
        <Typography fontWeight={700} sx={{ minWidth: 100, mt: 2 }}>
          카테고리
        </Typography>
        <Box sx={{ flex: 1 }}>
          <select
            value={form.category}
            onChange={e => setForm(prev => ({ ...prev, category: e.target.value }))}
            style={{
              width: '100%',
              height: 48,
              fontSize: 16,
              borderRadius: 8,
              border: '1px solid #ccc',
              padding: '0 12px'
            }}
            required
          >
            <option value="">카테고리 선택</option>
            {categoryOptions.map(opt => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </Box>
      </Box>
      <Divider sx={{ my: 3, borderColor: '#eee', borderWidth: 1.5 }} />

      {/* 설명 */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'flex-start', gap: 2 }}>
        <Typography fontWeight={700} sx={{ minWidth: 100, mt: 1 }}>
          설명
        </Typography>
        <TextField
          name="content"
          value={form.content}
          onChange={handleChange}
          required
          fullWidth
          multiline
          minRows={4}
          placeholder="상품 설명을 입력해 주세요."
          sx={{ flex: 1 }}
        />
      </Box>
      <Divider sx={{ my: 3, borderColor: '#eee', borderWidth: 1.5 }} />

      {/* 가격 */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Typography fontWeight={700} sx={{ minWidth: 100 }}>
          가격
        </Typography>
        <TextField
          name="price"
          type="text"
          value={form.price}
          onChange={handlePriceChange}
          required
          fullWidth
          InputProps={{
            startAdornment: <InputAdornment position="start">₩</InputAdornment>,
            inputProps: { min: 0 },
          }}
          sx={{ flex: 1 }}
        />
      </Box>

      {/* 등록/수정 버튼 */}
      <Box display="flex" justifyContent="flex-end">
        <Button
          type="submit"
          variant="contained"
          color={isEdit ? 'info' : 'primary'}
          size="large"
          disabled={loading}
          sx={{ width: 180, height: 48, fontWeight: 700, fontSize: 18, mt: 2 }}
        >
          {loading ? (isEdit ? '수정 중...' : '등록 중...') : isEdit ? '상품 수정' : '상품 등록'}
        </Button>
      </Box>
      <FormSnackbar open={openError} message={formError} onClose={() => setOpenError(false)} />
    </Box>
  );
};

export default ProductRegisterPage;