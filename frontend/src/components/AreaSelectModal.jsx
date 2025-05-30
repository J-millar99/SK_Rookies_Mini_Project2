import React, { useState } from 'react';
import emdData from '../json/emd_code.json';
import axios from 'axios';
import CloseRoundedIcon from '@mui/icons-material/CloseRounded';

const PAGE_SIZE = 20;
const KAKAO_REST_API_KEY = import.meta.env.VITE_KAKAO_REST_API_KEY; // 실제 키로 교체 필요

const AreaSelectModal = ({ onSelect, onClose, onReset }) => {
    const [area, setArea] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [page, setPage] = useState(0);
    const [myLocation, setMyLocation] = useState(null);
    const [locLoading, setLocLoading] = useState(false);
    const [locError, setLocError] = useState('');
    const [resetHover, setResetHover] = useState(false);
    const [hoveredIdx, setHoveredIdx] = useState(-1);



    const handleAreaChange = (e) => {
        const value = e.target.value.replace(/\s+/g, ' ').trim().toLowerCase();
        setArea(e.target.value);
        setPage(0);

        if (value.length < 2) {
            setSuggestions([]);
            setShowSuggestions(false);
            return;
        }

        const filtered = emdData.filter(item => {
            if (!item.읍면동명 || item.읍면동명.length === 0) return false;
            if (item.삭제일자 && item.삭제일자 !== "") return false;
            const full = [item.시도명, item.시군구명, item.읍면동명]
                .filter(Boolean)
                .join(' ')
                .replace(/\s+/g, ' ')
                .toLowerCase();
            return full.includes(value);
        });
        setSuggestions(filtered);
        setShowSuggestions(true);
    };

    const handleSuggestionClick = (suggestion) => {
        onSelect(suggestion);
    };

    // 내 위치 찾기 버튼 클릭 시
    const handleFindMyLocation = async () => {
        setLocLoading(true);
        setLocError('');
        setMyLocation(null);
        try {
            // // 1) 백엔드에서 주소 정보 받아오기
            // const res = await axios.get('/get-location');
            // const addressInfo = res.data.documents?.[0]?.address;
            // if (!addressInfo) throw new Error('주소 정보를 찾을 수 없습니다.');

            // // 2) 시도명, 시군구명, 읍면동명 추출 및 공백 기준 앞부분만
            // const 시도명 = addressInfo.region_1depth_name;
            // const 시군구명 = addressInfo.region_2depth_name;
            // let 읍면동명 = addressInfo.region_3depth_name;
            // if (읍면동명 && 읍면동명.includes(' ')) {
            //     읍면동명 = 읍면동명.split(' ')[0];
            // }

            // <test> 브라우저에서 직접 위경도 얻기
            const getPosition = () =>
                new Promise((resolve, reject) => {
                    if (!navigator.geolocation) {
                        reject(new Error('이 브라우저에서는 위치 정보를 지원하지 않습니다.'));
                    } else {
                        navigator.geolocation.getCurrentPosition(
                            pos => resolve(pos),
                            err => reject(err)
                        );
                    }
                });

            const pos = await getPosition();
            const lat = pos.coords.latitude;
            const lng = pos.coords.longitude;

            const kakaoRes = await axios.get(
                `https://dapi.kakao.com/v2/local/geo/coord2address.json?x=${lng}&y=${lat}`,
                {
                    headers: {
                        Authorization: `KakaoAK ${KAKAO_REST_API_KEY}`,
                    },
                }
            );

            const addressInfo = kakaoRes.data.documents[0]?.address;
            if (!addressInfo) throw new Error('주소 정보를 찾을 수 없습니다.');

            const 시도명 = addressInfo.region_1depth_name;
            const 시군구명 = addressInfo.region_2depth_name;
            let 읍면동명 = addressInfo.region_3depth_name;

            if (읍면동명 && 읍면동명.includes(' ')) {
                읍면동명 = 읍면동명.split(' ')[0];
            }
            // </test> end

            setMyLocation({
                시도명,
                시군구명,
                읍면동명,
                lat,
                lng,
            });
        } catch (err) {
            setLocError('내 위치를 찾을 수 없습니다.');
            console.error(err);
        } finally {
            setLocLoading(false);
        }
    };

    const pagedSuggestions = suggestions.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);
    const hasNext = (page + 1) * PAGE_SIZE < suggestions.length;
    const hasPrev = page > 0;

    return (
        <div
            style={{
                position: 'fixed',
                top: 0, left: 0, right: 0, bottom: 0,
                background: 'rgba(0,0,0,0.3)',
                zIndex: 1000,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}
            onClick={onClose}
        >
            <div
                style={{
                    background: '#fff',
                    padding: 24,
                    borderRadius: 8,
                    minWidth: 350,
                    maxWidth: 400,
                    boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
                    position: 'relative'
                }}
                onClick={e => e.stopPropagation()}
            >
                <input
                    type="text"
                    placeholder="지역명 입력"
                    value={area}
                    onChange={handleAreaChange}
                    style={{ width: '100%', marginBottom: 8, height: 40, borderRadius: 5 }}
                    autoFocus
                />
                
                {/* 내 위치 찾기 버튼 */}
                <button
                    style={{ width: '100%', marginBottom: 8, background: '#f0f0f0', padding: 8, borderRadius: 4 }}
                    onClick={handleFindMyLocation}
                    disabled={locLoading}
                >
                    {locLoading ? '내 위치 찾는 중...' : '내 위치 찾기'}
                </button>

                {/* 지역 선택 초기화 버튼 */}
                <button
                    style={{
                        width: '100%',
                        marginBottom: 8,
                        background: '#fff',
                        padding: 8,
                        borderRadius: 4,
                        border: `1px solid ${resetHover ? '#EA002C' : '#ccc'}`,
                        color: '#EA002C',
                        transition: 'border-color 0.2s'
                    }}
                    onMouseEnter={() => setResetHover(true)}
                    onMouseLeave={() => setResetHover(false)}
                    onClick={() => {
                        setArea('');
                        setSuggestions([]);
                        setShowSuggestions(false);
                        setMyLocation(null);
                        if (onReset) onReset();
                    }}
                >
                    지역 선택 초기화
                </button>

                {/* 내 위치 결과 시각화 */}
                {locError && <div style={{ color: 'red', marginBottom: 8 }}>{locError}</div>}
                {myLocation && (
                    <div style={{ marginBottom: 8, background: '#f9f9f9', padding: 8, borderRadius: 4 }}>
                        <button
                            style={{fontSize: 20, width: 350 }}
                            onClick={() => onSelect(myLocation)}>
                            내 위치: {[myLocation.시도명, myLocation.시군구명, myLocation.읍면동명]
                                .filter(Boolean).join(' ')}
                        </button>
                    </div>
                )}
                {showSuggestions && pagedSuggestions.length > 0 && (
                    <ul
                        style={{
                            border: '1px solid #ccc',
                            maxHeight: 200,
                            overflowY: 'auto',
                            margin: 0,
                            padding: 0,
                            listStyle: 'none',
                            background: '#fff',
                            position: 'relative'
                        }}
                    >
                        {pagedSuggestions.map((s, idx) => (
                            <li
                                key={idx}
                                style={{
                                    padding: 8,
                                    cursor: 'pointer',
                                    background: hoveredIdx === idx ? '#f0f0f0' : '#fff', // hover 시 배경색 변경
                                    transition: 'background 0.15s'
                                }}
                                onMouseEnter={() => setHoveredIdx(idx)}
                                onMouseLeave={() => setHoveredIdx(-1)}
                                onClick={() => handleSuggestionClick(s)}
                            >
                                {[s.시도명, s.시군구명, s.읍면동명].filter(Boolean).join(' ')}
                            </li>
                        ))}
                        <li
                            style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                padding: 4,
                                background: '#f9f9f9',
                                position: 'sticky',
                                bottom: 0,
                                borderTop: '1px solid #eee',
                                zIndex: 1
                            }}
                        >
                            <div>
                                {hasPrev && (
                                    <button
                                        type="button"
                                        onMouseDown={e => {
                                            e.preventDefault();
                                            e.stopPropagation();
                                            setPage(page - 1);
                                        }}>
                                        이전
                                    </button>
                                )}
                            </div>
                            <div style={{ marginLeft: 'auto' }}>
                                {hasNext && (
                                    <button
                                        type="button"
                                        onMouseDown={e => {
                                            e.preventDefault();
                                            e.stopPropagation();
                                            setPage(page + 1);
                                        }}>
                                        다음
                                    </button>
                                )}
                            </div>
                        </li>
                    </ul>
                )}
                <button
                    style={{
                        position: 'absolute',
                        top: 4,
                        right: 4,
                        padding: 0,
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                    }}
                    onClick={onClose}
                >
                    <CloseRoundedIcon fontSize="medium" />
                </button>
            </div>
        </div>
    );
};

export default AreaSelectModal;