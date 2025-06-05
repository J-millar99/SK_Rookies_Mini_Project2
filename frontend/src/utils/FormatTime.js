export function FormatTime(createdAt) {
  const now = new Date();
  // 한국시간이 Z(UTC)로 잘못 내려올 때 9시간 빼기 (임시방편)
  const created = new Date(new Date(createdAt).getTime() - 9 * 60 * 60 * 1000);

  const diffMs = now.getTime() - created.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const safeDiffSec = diffSec < 0 ? 0 : diffSec;

  const isSameDay =
    now.getFullYear() === created.getFullYear() &&
    now.getMonth() === created.getMonth() &&
    now.getDate() === created.getDate();

  if (isSameDay) {
    if (safeDiffSec < 60) return `${safeDiffSec}초 전`;
    const diffMin = Math.floor(safeDiffSec / 60);
    if (diffMin < 60) return `${diffMin}분 전`;
    const diffHour = Math.floor(diffMin / 60);
    return `${diffHour}시간 전`;
  } else {
    const y = String(created.getFullYear()).slice(2);
    const m = String(created.getMonth() + 1).padStart(2, '0');
    const d = String(created.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}