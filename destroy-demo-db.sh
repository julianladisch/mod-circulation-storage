#!/usr/bin/env bash

host=${1:-localhost}
port=${2:-5432}

executing_user=${4:-$USER}
executing_password=${5:-}

cd database-setup

./destroy-db.sh loan_demo demo_tenant_loan_storage loan_demo_admin ${host} ${port} ${executing_user} ${executing_password}

cd ..