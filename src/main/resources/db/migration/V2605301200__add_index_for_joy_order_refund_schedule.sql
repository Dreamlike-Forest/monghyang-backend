create index idx_joy_order_refund_schedule
    on joy_order (joy_payment_status, joy_id, is_deleted, reservation);
