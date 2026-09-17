# Cloud-Native Appointment Tracker System

Система микросервисной архитектуры для управления записью клиентов в салон красоты. Проект спроектирован с упором на отказоустойчивость, безопасность и надежность оркестрации в среде **Kubernetes**.

## 🏗️ Архитектура (Modernized)

Система развернута в кластере Kubernetes с использованием **Istio Service Mesh**. Вместо классического API Gateway, оркестрация бизнес-процессов реализована на **Temporal**. Трафик управляется через Istio Ingress Gateway.

```mermaid
graph LR
    User((Client)) --> |http| IGW[Istio Ingress Gateway]
    IGW --> |VirtualService| US[User Service]
    IGW --> |VirtualService| BS[Booking Service]
    IGW --> |VirtualService| NS[Notification Service]
    BS --> |Temporal Workflow| TS((Temporal Server))
    TS --> |Worker Execution| NS
    
    subgraph Data
        US --> DB1[(Postgres)]
        BS --> DB2[(Postgres)]
        US <--> Redis[(Redis)]
        BS <--> Redis
    end
    
    subgraph Observability
        Prom[Prometheus]
        Graf[Grafana]
        Lok[Loki]
        Kia[Kiali]
    end
    
    US & BS & NS --> Prom
    US & BS & NS --> Lok
    US & BS & NS -.-> Kia
    Prom --> Graf
    Lok --> Graf
    Kia --> |Mesh Viz| Prom
```

## 🚀 Основные особенности

- **Kubernetes-native**: Развертывание полностью управляется через Helm-чарты.
- **Service Mesh (Istio)**: Управление трафиком, принудительное mTLS-шифрование (STRICT mode), observability.
- **Temporal Orchestration**: Надежная оркестрация асинхронных бизнес-процессов (уведомления) с автоматическими ретраями, гарантией доставки и управлением состоянием.
- **Istio-as-Gateway**: Маршрутизация внешнего трафика напрямую в микросервисы средствами Istio VirtualService.
- **Observability**: Интегрированный стек мониторинга (Prometheus, Grafana, Loki).

## 🛠️ Технологический стек

*   **Core**: Java 21, Spring Boot 3
*   **Orchestration**: Temporal (Workflow & Activities)
*   **Infrastructure**: Kubernetes, Helm, Istio
*   **Database**: PostgreSQL
*   **Caching**: Redis
*   **Observability**: Prometheus, Grafana, Loki

## ⚙️ Инструкция по развертыванию

1. **Подготовка кластера**:
   Убедитесь, что у вас запущен кластер Kubernetes (например, Minikube).

2. **Установка Istio**:
   Если `istioctl` не добавлен в PATH, используйте путь к файлу из проекта:
   ```bash
   # Для Windows (PowerShell):
   .\istio-1.23.0\bin\istioctl.exe install --set profile=demo -y
   
   # Или если istioctl уже в PATH:
   istioctl install --set profile=demo -y
   
   kubectl label namespace default istio-injection=enabled
   ```

3. **Создание секретов**:
   Создайте секрет для паролей баз данных, который ожидает приложение:
   ```bash
   kubectl create secret generic db-passwords --from-literal=user-db-password=secret_password --from-literal=booking-db-password=booking_password
   ```

4. **Установка приложения**:
   ```bash
   helm install salon-app ./salon-chart/
   ```

5. **Доступ к системе**:
   Запустите туннель Minikube для доступа к Ingress Gateway:
   ```bash
   minikube tunnel
   ```
   Система будет доступна по адресу `http://localhost/api/...`

## 🛡️ Инженерные решения
*   **Temporal вместо RabbitMQ**: Переход на Temporal позволил отказаться от сложной логики очередей и ручной реализации компенсирующих транзакций. Temporal гарантирует надежную доставку событий и управление состоянием воркфлоу, избавляя от необходимости внедрения дополнительных паттернов атомарности (Transactional Outbox).
*   **Service Mesh**: Использование Istio позволило вынести логику маршрутизации и безопасности из кода микросервисов в инфраструктурный слой.
*   **mTLS Enforcement**: Безопасность коммуникаций внутри кластера обеспечивается политикой `PeerAuthentication` в режиме `STRICT` (файл `salon-chart/templates/mtls.yaml`), что гарантирует, что весь внутренний трафик между микросервисами зашифрован и взаимно аутентифицирован.
