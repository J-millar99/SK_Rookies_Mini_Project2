import axios from 'axios';

const RESERVATION_API_BASE_URL = 'http://localhost:8080/api/reservations';

const reservationService = {
  // 거래 요청(예약)을 생성
  createReservation: async (productId) => {
    try {
      const response = await axios.post(RESERVATION_API_BASE_URL, {
        productId: productId,
      });
      return { success: true, data: response.data.data };
    } catch (error) {
      console.error('거래 요청 생성 실패:', error.response?.data || error.message);
      return { success: false, error: error.response?.data || error.message };
    }
  },

  // 예약 상태를 업데이트
  updateReservationStatus: async (reservationId, status) => {
    try {
      const response = await axios.patch(`${RESERVATION_API_BASE_URL}/${reservationId}/status`, {
        status: status,
      });
      return { success: true, data: response.data.data };
    } catch (error) {
      console.error(
        `예약 ID ${reservationId}의 상태를 ${status}로 업데이트하는 데 실패했습니다:`,
        error
      );
      return { success: false, error: error.response?.data || error.message };
    }
  },
};

export default reservationService;

export const { createReservation, updateReservationStatus } = reservationService;
