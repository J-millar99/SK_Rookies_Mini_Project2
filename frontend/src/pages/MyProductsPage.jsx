import React, { useEffect, useState } from "react";
import axios from "axios";
import { Box, Typography, Divider } from "@mui/material";
import ProductsList from "../components/ProductsList";
import { FormatTime } from "../utils/FormatTime";
import { useNavigate } from "react-router-dom";

const MyProductsPage = () => {
    const [products, setProducts] = useState([]);
    const token = localStorage.getItem("accessToken");
    const navigate = useNavigate();

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    useEffect(() => {
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
                alert("내 상품 목록을 불러올 수 없습니다.");
            });
    }, [token]);

    // 상품 클릭 시 상세 페이지로 이동
    const handleProductClick = (id) => {
        navigate(`/my-products/${id}`);
    };

    return (
        <Box sx={{ width: 1200, mx: "auto", p: 3 }}>
            <Typography variant="h4" fontWeight={700} mb={4} align="left">
                내가 올린 상품
            </Typography>
            <Divider sx={{ mb: 4, borderColor: "#222", borderWidth: 2 }} />
            <ProductsList
                products={products}
                formatTime={FormatTime}
                onProductClick={handleProductClick}
            />
        </Box>
    );
};

export default MyProductsPage;