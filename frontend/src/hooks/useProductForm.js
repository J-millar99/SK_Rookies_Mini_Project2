import { useState } from "react";
import { PriceInput } from "../utils/PriceInput";

const MAX_IMAGES = 10;
const MAX_PRODUCTS = 5;
const BANNED_WORDS = ["마약", "위조", "불법"];

export default function useProductForm({ isEdit, id, userProfile, userProductCount, navigate }) {
  const [form, setForm] = useState({
    title: "",
    content: "",
    price: "",
    category: "",
    images: [],
  });
  const [formError, setFormError] = useState("");
  const [openError, setOpenError] = useState(false);
  const [loading, setLoading] = useState(false);

  const isBannedProduct = (title, content) =>
    BANNED_WORDS.some(word => title.includes(word) || content.includes(word));

  const handleChange = e => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handlePriceChange = e => {
    setForm(prev => ({ ...prev, price: PriceInput(e.target.value) }));
  };

  const showError = msg => {
    setFormError(msg);
    setOpenError(true);
  };

  const validate = () => {
    if (form.images.length > MAX_IMAGES) {
      showError(`이미지는 최대 ${MAX_IMAGES}장까지 등록할 수 있습니다.`);
      return false;
    }
    if (!isEdit && userProductCount >= MAX_PRODUCTS) {
      showError("등록 가능한 상품 개수를 초과했습니다.");
      return false;
    }
    if (isBannedProduct(form.title, form.content)) {
      showError("금지 품목은 등록할 수 없습니다.");
      return false;
    }
    if (userProfile.is_banned === "true") {
      showError("정지된 계정은 상품을 등록할 수 없습니다.");
      return false;
    }
    if (!form.category || form.category === "") {
      showError("카테고리를 선택해 주세요.");
      return false;
    }
    return true;
  };

  return {
    form, setForm,
    formError, setFormError,
    openError, setOpenError,
    loading, setLoading,
    handleChange,
    handlePriceChange,
    validate,
    showError,
  };
}
