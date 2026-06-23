insert into app_users (username) values ('demo');

insert into wallet_balances (user_id, currency, balance)
select id, 'USDT', 50000.00000000
from app_users
where username = 'demo';

insert into wallet_balances (user_id, currency, balance)
select id, 'BTC', 0.00000000
from app_users
where username = 'demo';

insert into wallet_balances (user_id, currency, balance)
select id, 'ETH', 0.00000000
from app_users
where username = 'demo';
