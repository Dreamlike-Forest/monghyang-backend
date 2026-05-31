package com.example.monghyang.domain.global;

/**
 * 프로젝트에서 사용하는 요일 정보를 나타내는 Enum입니다.
 */
public enum DayOfWeek {
    Mon, Tue, Wed, Thu, Fri, Sat, Sun;

    /**
     * java.time.DayOfWeek를 프로젝트 DayOfWeek로 변환합니다.
     *
     * @param jdkDayOfWeek JDK의 요일 Enum
     * @return 변환된 프로젝트 DayOfWeek Enum (null 입력 시 null 반환)
     */
    public static DayOfWeek from(java.time.DayOfWeek jdkDayOfWeek) {
        if (jdkDayOfWeek == null) {
            return null;
        }
        switch (jdkDayOfWeek) {
            case MONDAY: return Mon;
            case TUESDAY: return Tue;
            case WEDNESDAY: return Wed;
            case THURSDAY: return Thu;
            case FRIDAY: return Fri;
            case SATURDAY: return Sat;
            case SUNDAY: return Sun;
            default: throw new IllegalArgumentException("지원하지 않는 요일입니다: " + jdkDayOfWeek);
        }
    }
}
