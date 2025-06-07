package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.AreaDTO;
import com.miniproject.rookiejangter.entity.Area;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.AreaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private AreaService areaService;

    @Test
    @DisplayName("지역 생성 테스트")
    void createAreaTest() {
        // Given
        String areaName = "서울";
        Area savedArea = Area.builder()
                .areaId(1)
                .areaName(areaName)
                .build();
        when(areaRepository.save(any(Area.class))).thenReturn(savedArea);

        // When
        AreaDTO.Response response = areaService.createArea(areaName);

        // Then
        assertThat(response.getAreaId()).isEqualTo(1);
        assertThat(response.getAreaName()).isEqualTo(areaName);
        verify(areaRepository, times(1)).save(any(Area.class));
    }

    @Test
    @DisplayName("ID로 지역 조회 성공 테스트")
    void getAreaByIdSuccessTest() {
        // Given
        Integer areaId = 1;
        String areaName = "부산";
        Area foundArea = Area.builder()
                .areaId(areaId)
                .areaName(areaName)
                .build();
        when(areaRepository.findById(areaId)).thenReturn(Optional.of(foundArea));

        // When
        AreaDTO.Response response = areaService.getAreaById(areaId);

        // Then
        assertThat(response.getAreaId()).isEqualTo(areaId);
        assertThat(response.getAreaName()).isEqualTo(areaName);
        verify(areaRepository, times(1)).findById(areaId);
    }

    @Test
    @DisplayName("ID로 지역 조회 실패 테스트 (BusinessException 발생)")
    void getAreaByIdNotFoundTest() {
        // Given
        Integer invalidAreaId = 999;
        when(areaRepository.findById(invalidAreaId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> areaService.getAreaById(invalidAreaId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AREA_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.AREA_NOT_FOUND.formatMessage(invalidAreaId));
        verify(areaRepository, times(1)).findById(invalidAreaId);
    }

    @Test
    @DisplayName("모든 지역 조회 테스트")
    void getAllAreasTest() {
        // Given
        List<Area> areas = Arrays.asList(
                Area.builder().areaId(1).areaName("서울").build(),
                Area.builder().areaId(2).areaName("대전").build()
        );
        when(areaRepository.findAll()).thenReturn(areas);

        // When
        List<AreaDTO.Response> responses = areaService.getAllAreas();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getAreaName()).isEqualTo("서울");
        assertThat(responses.get(1).getAreaName()).isEqualTo("대전");
        verify(areaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("지역 정보 수정 성공 테스트")
    void updateAreaSuccessTest() {
        // Given
        Integer areaId = 1;
        String existingAreaName = "인천";
        String newAreaName = "제주";
        Area existingArea = Area.builder()
                .areaId(areaId)
                .areaName(existingAreaName)
                .build();
        Area updatedArea = Area.builder()
                .areaId(areaId)
                .areaName(newAreaName)
                .build();
        when(areaRepository.findById(areaId)).thenReturn(Optional.of(existingArea));
        when(areaRepository.save(any(Area.class))).thenReturn(updatedArea);

        // When
        AreaDTO.Response response = areaService.updateArea(areaId, newAreaName);

        // Then
        assertThat(response.getAreaId()).isEqualTo(areaId);
        assertThat(response.getAreaName()).isEqualTo(newAreaName);
        verify(areaRepository, times(1)).findById(areaId);
        verify(areaRepository, times(1)).save(any(Area.class));
    }

    @Test
    @DisplayName("지역 정보 수정 실패 테스트 (BusinessException 발생)")
    void updateAreaNotFoundTest() {
        // Given
        Integer invalidAreaId = 999;
        String newAreaName = "강릉";
        when(areaRepository.findById(invalidAreaId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> areaService.updateArea(invalidAreaId, newAreaName));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AREA_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.AREA_NOT_FOUND.formatMessage(invalidAreaId));
        verify(areaRepository, times(1)).findById(invalidAreaId);
        verify(areaRepository, never()).save(any(Area.class));
    }

    @Test
    @DisplayName("지역 삭제 성공 테스트")
    void deleteAreaSuccessTest() {
        // Given
        Integer areaId = 1;
        Area existingArea = Area.builder()
                .areaId(areaId)
                .areaName("울산")
                .build();
        when(areaRepository.findById(areaId)).thenReturn(Optional.of(existingArea));
        doNothing().when(areaRepository).delete(existingArea);

        // When
        areaService.deleteArea(areaId);

        // Then
        verify(areaRepository, times(1)).findById(areaId);
        verify(areaRepository, times(1)).delete(existingArea);
    }

    @Test
    @DisplayName("지역 삭제 실패 테스트 (BusinessException 발생)")
    void deleteAreaNotFoundTest() {
        // Given
        Integer invalidAreaId = 999;
        when(areaRepository.findById(invalidAreaId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> areaService.deleteArea(invalidAreaId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AREA_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.AREA_NOT_FOUND.formatMessage(invalidAreaId));
        verify(areaRepository, times(1)).findById(invalidAreaId);
        verify(areaRepository, never()).delete(any());
    }
}