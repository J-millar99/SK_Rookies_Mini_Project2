import React, { useEffect, useState } from "react";
import axios from "axios";
import { Box, Typography, Divider } from "@mui/material";
import ProductsList from "../components/ProductsList";
import { FormatTime } from "../utils/FormatTime";
import { useNavigate, useLocation } from "react-router-dom";
import FormSnackbar from "../components/FormSnackbar";

const MyProductsPage = () => {
    const [products, setProducts] = useState([]);
    const token = localStorage.getItem("accessToken");
    const navigate = useNavigate();
    const location = useLocation();

    // 찜한 상품 페이지 여부
    const isDibsPage = location.state?.dibs === true;

    // snackbar 상태
    const snackbarState = location.state?.snackbar;
    const [snackbar, setSnackbar] = useState(
        snackbarState || { open: false, message: "", severity: "info" }
    );

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    useEffect(() => {
        if (!token) {
            setSnackbar({
                open: true,
                message: "로그인이 필요합니다.",
                severity: "error",
            });
            return;
        }
        if (isDibsPage) {
            // 찜한 상품 목록 불러오기
            axios
                .get("http://localhost:8080/api/dibs", {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                })
                .then(async (res) => {
                    console.log("찜한 상품 응답:", res.data);
                    // content 배열에서 productId만 추출
                    const contentArr = Array.isArray(res.data.data?.content) ? res.data.data.content : [];
                    // productId 배열
                    const productIds = contentArr.map(item => item.productId);

                    // 각 productId로 상품 상세 정보 요청 (병렬)
                    const productDetails = await Promise.all(
                        productIds.map(id =>
                            axios.get(`http://localhost:8080/api/products/${id}`)
                                .then(r => r.data.data)
                                .catch(() => null)
                        )
                    );
                    // null이 아닌 상품만
                    setProducts(productDetails.filter(Boolean));
                })
                .catch(() => {
                    setSnackbar({
                        open: true,
                        message: "찜한 상품 목록을 불러올 수 없습니다.",
                        severity: "error",
                    });
                });
        } else {
            // 내가 올린 상품 목록 불러오기
            axios
                .get("http://localhost:8080/api/users/products", {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                })
                .then((res) => {
                    const productsArr = Array.isArray(res.data.content) ? res.data.content : [];
                    setProducts(productsArr);
                })
                .catch(() => {
                    setSnackbar({
                        open: true,
                        message: "내 상품 목록을 불러올 수 없습니다.",
                        severity: "error",
                    });
                });
        }
    }, [token, isDibsPage]);

    // location.state로 온 snackbar 메시지는 한 번만 보여주고 지워줌
    useEffect(() => {
        if (snackbarState) {
            setSnackbar(snackbarState);
            window.history.replaceState({}, document.title); // state 초기화
        }
    }, [snackbarState]);

    // 상품 클릭 시 상세 페이지로 이동
    const handleProductClick = (id) => {
        navigate(`/products/${id}`);
    };

    return (
        <Box sx={{ width: 1200, mx: "auto", p: 3 }}>
            <Typography variant="h4" fontWeight={700} mb={4} align="left">
                {isDibsPage ? "내가 찜한 상품" : "내가 올린 상품"}
            </Typography>
            <Divider sx={{ mb: 4, borderColor: "#222", borderWidth: 2 }} />
            <ProductsList
                products={products}
                formatTime={FormatTime}
                onProductClick={handleProductClick}
            />
            <FormSnackbar
                open={snackbar.open}
                message={snackbar.message}
                severity={snackbar.severity}
                onClose={() => setSnackbar({ ...snackbar, open: false })}
            />
        </Box>
    );
};

export default MyProductsPage;