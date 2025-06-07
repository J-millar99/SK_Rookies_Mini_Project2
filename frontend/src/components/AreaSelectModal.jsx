import React, { useState, useEffect } from 'react';
import axios from 'axios';
import CloseRoundedIcon from '@mui/icons-material/CloseRounded';
import FormSnackbar from '../components/FormSnackbar';
import {
  Box,
  Button,
  TextField,
  List,
  ListItem,
  Typography,
  useTheme,
  CircularProgress,
  Paper,
  IconButton,
} from '@mui/material';
import { getAreas } from '../api/area'; // 추가


const KAKAO_REST_API_KEY = import.meta.env.VITE_KAKAO_REST_API_KEY;

const REGION_TO_AREANAME = {
  '서울': '서울특별시',
  '부산': '부산광역시',
  '대구': '대구광역시',
  '인천': '인천광역시',
  '광주': '광주광역시',
  '대전': '대전광역시',
  '울산': '울산광역시',
  '세종': '세종특별자치시',
  '경기': '경기도',
  '강원': '강원특별자치도',
  '충북': '충청북도',
  '충남': '충청남도',
  '전북': '전북특별자치도',
  '전남': '전라남도',
  '경북': '경상북도',
  '경남': '경상남도',
  '제주': '제주특별자치도',
};

const AreaSelectModal = ({ onSelect, onClose, onReset }) => {
  const [area, setArea] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [myLocation, setMyLocation] = useState(null);
  const [locLoading, setLocLoading] = useState(false);
  const [locError, setLocError] = useState('');
  const [hoveredIdx, setHoveredIdx] = useState(-1);
  const [fetchError, setFetchError] = useState(false);
  const [sidoList, setSidoList] = useState([]);
  const theme = useTheme();

  useEffect(() => {
    getAreas()
      .then(data => {
        const arr = Array.isArray(data.data) ? data.data : [];
        setSidoList(arr.map(item => ({
          areaId: item.areaId ?? item.value,
          areaName: item.areaName ?? item.label
        })));
      })
      .catch(() => {
        setFetchError(true);
      });
  }, []);

  const handleAreaChange = (e) => {
    const value = e.target.value.replace(/\s+/g, ' ').trim().toLowerCase();
    setArea(e.target.value);

    if (value.length < 1) {
      setSuggestions([]);
      return;
    }
    const filtered = sidoList.filter(item => {
      if (!item.areaName || item.areaName.length === 0) return false;
      return item.areaName.toLowerCase().includes(value);
    });
    setSuggestions(filtered);
  };

  const handleSuggestionClick = (suggestion) => {
    onSelect({ areaName: suggestion.areaName });
  };

  const handleFindMyLocation = async () => {
    setLocLoading(true);
    setLocError('');
    setMyLocation(null);
    try {
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

      const region1 = addressInfo.region_1depth_name;
      const areaName = REGION_TO_AREANAME[region1] || region1;

      setMyLocation({
        areaName,
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

  return (
    <Box
      sx={{
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
      <Paper
        sx={{
          background: '#fff',
          p: 3,
          borderRadius: 2,
          minWidth: 350,
          maxWidth: 400,
          boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
          position: 'relative'
        }}
        onClick={e => e.stopPropagation()}
      >
        <TextField
          fullWidth
          placeholder="시도명 입력"
          value={area}
          onChange={handleAreaChange}
          size="small"
          sx={{ mb: 1 }}
          autoFocus
        />

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {/* 내 위치 찾기 버튼 */}
          <Button
            variant="outlined"
            color="info"
            fullWidth
            sx={{ mb: 1, fontWeight: 700, borderRadius: 1,
                '&:hover': {
                    borderColor: theme.palette.info.main,
                    background: theme.palette.info.extraLight,
                },
             }}
            onClick={handleFindMyLocation}
            disabled={locLoading}
            startIcon={locLoading && <CircularProgress size={18} />}
          >
            {locLoading ? '내 위치 찾는 중...' : '내 위치 찾기'}
          </Button>

          {/* 지역 선택 초기화 버튼 */}
          <Button
            variant="outlined"
            color="error"
            fullWidth
            sx={{
              mb: 1,
              fontWeight: 700,
              borderRadius: 1,
              transition: 'border-color 0.2s',
              '&:hover': {
                borderColor: theme.palette.error.main,
                background: theme.palette.error.extraLight,
              },
            }}
            onClick={() => {
              setArea('');
              setSuggestions([]);
              setMyLocation(null);
              if (onReset) onReset();
            }}
          >
            지역 선택 초기화
          </Button>
        </Box>

        {/* 내 위치 결과 시각화 */}
        {locError && (
          <Typography color="error" sx={{ mb: 1 }}>
            {locError}
          </Typography>
        )}
        {myLocation && (
          <Box sx={{ mb: 1, background: theme.palette.background.default, p: 1, borderRadius: 1 }}>
            <Button
              fullWidth
              variant="outlined"
              color="primary"
              sx={{ fontSize: 18, fontWeight: 700 }}
              onClick={() => onSelect({ areaName: myLocation.areaName })}
            >
              내 위치: {myLocation.areaName}
            </Button>
          </Box>
        )}

        <List
          sx={{
            border: '1px solid #ccc',
            maxHeight: 200,
            overflowY: 'auto',
            mt: 1,
            mb: 1,
            bgcolor: '#fff',
            position: 'relative',
            borderRadius: 1,
            p: 0,
          }}
        >
          {(area.trim().length > 0 ? suggestions : sidoList).map((s, idx) => (
            <ListItem
              key={s.areaId || idx}
              button
              selected={hoveredIdx === idx}
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(-1)}
              onClick={() => handleSuggestionClick(s)}
              sx={{
                p: 1,
                cursor: 'pointer',
                bgcolor: hoveredIdx === idx ? theme.palette.background.default : '#fff',
                transition: 'background 0.15s'
              }}
            >
              <Typography>{s.areaName}</Typography>
            </ListItem>
          ))}
        </List>

        <IconButton
          sx={{
            position: 'absolute',
            top: 4,
            right: 4,
            color: theme.palette.grey[700],
          }}
          onClick={onClose}
        >
          <CloseRoundedIcon fontSize="medium" />
        </IconButton>
        <FormSnackbar
          open={fetchError}
          message="지역 정보를 불러올 수 없습니다."
          onClose={() => setFetchError(false)}
        />
      </Paper>
    </Box>
  );
};

export default AreaSelectModal;