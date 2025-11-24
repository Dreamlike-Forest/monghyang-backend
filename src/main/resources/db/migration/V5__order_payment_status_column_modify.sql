alter table order_status_history modify column to_status enum('FAILED', 'PAID', 'PENDING', 'CANCELED') not null;

alter table orders modify column payment_status enum('FAILED', 'PAID', 'PENDING', 'CANCELED') not null;
