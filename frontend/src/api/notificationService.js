import axios from 'axios';

const notificationService = {
  /**
   * 사용자의 알림 목록을 페이지네이션하여 가져옵니다.
   * 백엔드 GET /api/notifications?page={page}&size={size}&sort={sort}
   * @param {number} page - 요청할 페이지 번호 (0부터 시작)
   * @param {number} size - 페이지당 알림 개수
   * @param {string} sortBy - 정렬 기준 (예: 'sentAt,desc')
   * @returns {Promise<object>} 백엔드의 Page 객체 응답 (content, totalElements, totalPages 등 포함)
   */
  getNotifications: async (page = 0, size = 10, sortBy = 'sentAt,desc') => {
    try {
      // baseURL이 axios 전역 설정에 되어 있거나, 완전한 URL을 사용해야 합니다.
      // 프로젝트의 axios 설정에 baseURL이 있다면 '/notifications'만 사용해도 됩니다.
      // 없다면, 'http://localhost:8080/api/notifications'와 같이 전체 경로를 명시해야 합니다.
      const response = await axios.get('http://localhost:8080/api/notifications', {
        params: {
          page: page,
          size: size,
          sort: sortBy,
        },
      });
      return { success: true, data: response.data };
    } catch (error) {
      console.error('알림 목록을 가져오는 데 실패했습니다:', error);
      return { success: false, error: error.response?.data || error.message };
    }
  },

  /**
   * 읽지 않은 알림의 개수를 가져오는 함수.
   * 백엔드 GET /api/notifications/unread-count
   * @returns {Promise<{success: boolean, data?: number, error?: string}>} 읽지 않은 알림 개수
   */
  getUnreadNotificationsCount: async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/notifications/unread-count'); // 경로는 '/api/notifications/unread-count'로 수정
      return { success: true, data: response.data };
    } catch (error) {
      console.error('읽지 않은 알림 수를 가져오는 데 실패했습니다:', error);
      return { success: false, error: error.response?.data || error.message };
    }
  },

  /**
   * 특정 알림을 읽음 처리하는 함수.
   * 백엔드 PATCH /api/notifications/{notificationId}/read
   * @param {number} notificationId - 읽음 처리할 알림의 ID
   * @returns {Promise<object>} 성공 여부
   */
  markNotificationAsRead: async (notificationId) => {
    try {
      await axios.patch(`http://localhost:8080/api/notifications/${notificationId}/read`); // 경로는 `/api/notifications/{notificationId}/read`로 수정
      return { success: true };
    } catch (error) {
      console.error('알림을 읽음 처리하는 데 실패했습니다:', error);
      return { success: false, error: error.response?.data || error.message };
    }
  },

  /**
   * 특정 알림을 삭제하는 함수.
   * 백엔드 DELETE /api/notifications/{notificationId}
   * @param {number} notificationId - 삭제할 알림의 ID
   * @returns {Promise<object>} 성공 여부
   */
  deleteNotification: async (notificationId) => {
    try {
      await axios.delete(`http://localhost:8080/api/notifications/${notificationId}`); // 경로는 `/api/notifications/{notificationId}`로 수정
      return { success: true };
    } catch (error) {
      console.error('알림을 삭제하는 데 실패했습니다:', error);
      return { success: false, error: error.response?.data || error.message };
    }
  },

  /**
   * 예약 상태를 업데이트하는 함수.
   * 백엔드 PATCH /api/reservation/{reservationId}/status
   * @param {number} reservationId - 상태를 변경할 예약의 ID
   * @param {string} status - 변경할 예약 상태 (예: 'ACCEPTED', 'DECLINED')
   * @returns {Promise<object>} 성공 여부 및 업데이트된 예약 데이터
   */
  updateReservationStatus: async (reservationId, status) => {
    try {
      const response = await axios.patch(
        `http://localhost:8080/api/reservations/${reservationId}/status`,
        { status: status }
      );
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

export default notificationService;

export const {
  getNotifications,
  getUnreadNotificationsCount,
  markNotificationAsRead,
  deleteNotification,
  updateReservationStatus,
} = notificationService;
