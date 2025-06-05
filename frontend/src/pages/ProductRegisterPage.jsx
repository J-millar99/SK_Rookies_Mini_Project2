import React, { useEffect } from "react";
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import {
  Box,
  Button,
  Divider,
  InputAdornment,
  Paper,
  TextField,
  Typography,
} from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import CategorySelect from "../components/CategorySelect";
import ProductImageUploader from "../components/ProductImageUploader";
import { CATEGORY_OPTIONS } from "../constants/CategoryOptions";
import { PriceInput } from "../utils/PriceInput";
import FormErrorSnackbar from "../components/FormErrorSnackbar";
import useProductForm from "../hooks/useProductForm";

const MAX_IMAGES = 10;


const ProductRegisterPage = ({ editMode }) => {
    const { id } = useParams();
    const isEdit = !!id || editMode;

    const navigate = useNavigate();
    const [userProfile, setUserProfile] = React.useState({ is_banned: "false" });
    const [userProductCount, setUserProductCount] = React.useState(0);

    const {
        form, setForm,
        formError, setFormError,
        openError, setOpenError,
        loading, setLoading,
        handleChange,
        handlePriceChange,
        validate,
        showError,
    } = useProductForm({ isEdit, id, userProfile, userProductCount, navigate });

    // accessToken에서 userId 추출
    const token = localStorage.getItem("accessToken");
    let userId = "";
    if (token) {
        try {
            const payload = jwtDecode(token);
            userId = payload.sub; // JWT의 sub가 userId
        } catch (e) {
            userId = "";
        }
    }

    // 상품 정보 불러오기 (수정 모드)
    useEffect(() => {
        if (isEdit && !id) {
            NotFound();
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
                .then((res) => {
                    const data = res.data;
                    // categoryId가 없고 categoryName만 있을 때, id를 CATEGORY_OPTIONS에서 역으로 찾기
                    let categoryValue = "";
                    if (data.categoryId !== undefined && data.categoryId !== null) {
                        categoryValue = String(data.categoryId);
                    } else if (data.category !== undefined && data.category !== null) {
                        categoryValue = String(data.category);
                    } else if (data.categoryName) {
                        const found = CATEGORY_OPTIONS.find(opt => opt.label === data.categoryName);
                        if (found) categoryValue = String(found.value);
                    }
                    setForm({
                        title: data.title || "",
                        content: data.content || "",
                        price: data.price ? data.price.toLocaleString() : "",
                        category: categoryValue,
                        images: Array.isArray(data.images)
                            ? data.images.map(img => ({
                                file: null,
                                url: img.imageUrl,
                                imageId: img.imageId,
                              }))
                            : [],
                    });
                })
                .catch(() => {
                    alert("상품 정보를 불러올 수 없습니다.");
                    navigate("/my-products");
                })
                .finally(() => setLoading(false));
        }
    }, [isEdit, id, token, navigate]);

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
                .catch(err => console.error("사용자 상태 정보 불러오기 실패", err));
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
                .catch(err => console.error("사용자의 상품 개수 불러오기 실패", err));
        }
    }, [userId, token]);
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        setFormError("");
        setOpenError(false);
        if (!validate()) return;
        setLoading(true);
        try {
            if (isEdit) {
                const product_id = id;
                const updateData = {
                    title: form.title,
                    content: form.content,
                    price: parseInt(form.price.replace(/,/g, ""), 10),
                    categoryId: parseInt(form.category, 10),
                };
                await axios.put(
                    `http://localhost:8080/api/users/products/${product_id}`,
                    updateData,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                            "Content-Type": "application/json",
                        },
                    }
                );
                alert("상품이 수정되었습니다.");
                navigate(`/my-products/${product_id}`);
            } else {
                const formData = new FormData();
                formData.append("title", form.title);
                formData.append("content", form.content);
                formData.append("price", parseInt(form.price.replace(/,/g, ""), 10));
                formData.append("categoryId", String(form.category));
                form.images.forEach((img) => {
                    if (img.file) {
                        formData.append("images", img.file);
                    }
                });
                const res = await axios.post(
                    `http://localhost:8080/api/users/products`,
                    formData,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                            "Content-Type": "multipart/form-data",
                        },
                    }
                );
                if (res.data && res.data.id) {
                    alert("성공적으로 등록되었습니다.");
                    navigate("/my-products");
                }
            }
        } catch (e) {
            if (e.response && e.response.status === 422) {
                showError("비즈니스 규칙 위반!");
            } else {
                showError(isEdit ? "상품 수정 중 오류가 발생했습니다." : "상품 등록 중 오류가 발생했습니다.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ width: 1100, mx: "auto", p: 3 }}>
            <Typography variant="h4" fontWeight={700} mb={4} ml={4} align="left">
                {isEdit ? "상품 수정" : "상품 등록"}
            </Typography>
            <Divider sx={{ mb: 4, borderColor: "#222", borderWidth: 2 }} />

            {/* 상품이미지 */}
            <Box sx={{ mb: 4 }}>
                {/* 이미지 업로더 컴포넌트 */}
                <ProductImageUploader
                    images={form.images}
                    onChange={imgs => setForm(prev => ({ ...prev, images: imgs }))}
                    maxImages={MAX_IMAGES}
                />
            </Box>
            <Divider sx={{ my: 3, borderColor: "#eee", borderWidth: 1.5 }} />

            {/* 상품명 */}
            <Box sx={{ mb: 4, display: "flex", alignItems: "center", gap: 2 }}>
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
                    inputProps={{ maxLength: 40 }}
                    sx={{ flex: 1 }}
                />
                <Typography variant="body2" color="#888" sx={{ ml: 1 }}>
                    {form.title.length}/40
                </Typography>
            </Box>
            <Divider sx={{ my: 3, borderColor: "#eee", borderWidth: 1.5 }} />

            {/* 카테고리 */}
            <Box sx={{ mb: 4, display: "flex", alignItems: "flex-start", gap: 2 }}>
                <Typography fontWeight={700} sx={{ minWidth: 100, mt: 2 }}>
                    카테고리
                </Typography>
                <Box sx={{ flex: 1 }}>
                    <CategorySelect
                        value={form.category}
                        onChange={e => setForm(prev => ({ ...prev, category: String(e.target.value) }))
                        }
                        options={CATEGORY_OPTIONS}
                        showIcon={false}
                    />
                </Box>
            </Box>
            <Divider sx={{ my: 3, borderColor: "#eee", borderWidth: 1.5 }} />

            {/* 설명 */}
            <Box sx={{ mb: 4, display: "flex", alignItems: "flex-start", gap: 2 }}>
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
            <Divider sx={{ my: 3, borderColor: "#eee", borderWidth: 1.5 }} />

            {/* 가격 */}
            <Box sx={{ mb: 4, display: "flex", alignItems: "center", gap: 2 }}>
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
                    color={isEdit ? "info" : "primary"}
                    size="large"
                    disabled={loading}
                    sx={{ width: 180, height: 48, fontWeight: 700, fontSize: 18, mt: 2 }}
                >
                    {loading ? (isEdit ? "수정 중..." : "등록 중...") : (isEdit ? "상품 수정" : "상품 등록")}
                </Button>
            </Box>
            {/* 에러 메시지 팝업(FormErrorSnackbar) */}
            <FormErrorSnackbar
                open={openError}
                message={formError}
                onClose={() => setOpenError(false)}
            />
        </Box>
    );
};

export default ProductRegisterPage;