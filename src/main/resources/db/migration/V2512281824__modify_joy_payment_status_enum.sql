-- JoyPaymentStatus enum에 새로운 값 추가

alter table joy_order modify joy_payment_status enum(
    'PENDING',
    'PAID',
    'CANCELED',
    'FAILED',
    'REFUND_REQUESTED',
    'REFUND_PROCESSING',
    'REFUND_FAILED',
    'REFUNDED'
    ) not null;

alter table joy_status_history modify to_status enum(
    'PENDING',
    'PAID',
    'CANCELED',
    'FAILED',
    'REFUND_REQUESTED',
    'REFUND_PROCESSING',
    'REFUND_FAILED',
    'REFUNDED'
    ) not null;