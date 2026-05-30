import apiClient from './apiClient'

/**
 * Promo kod API (F13). Backend: reservation-service /api/promo-codes.
 * validateByCode dohvaca kod po stringu — 404 ako ne postoji.
 */
export const promoApi = {
  validateByCode: async (code) => {
    const response = await apiClient.get(`/api/promo-codes/code/${encodeURIComponent(code)}`)
    return response.data  // PromoCodeResponseDTO
  }
}
