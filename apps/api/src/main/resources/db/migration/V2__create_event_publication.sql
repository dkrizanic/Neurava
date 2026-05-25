CREATE TABLE IF NOT EXISTS event_publication (
  completion_attempts integer NOT NULL,
  completion_date timestamp(6) with time zone,
  last_resubmission_date timestamp(6) with time zone,
  publication_date timestamp(6) with time zone NOT NULL,
  id uuid NOT NULL,
  event_type varchar(255) NOT NULL,
  listener_id varchar(255) NOT NULL,
  serialized_event varchar(255) NOT NULL,
  status varchar(255) CHECK (
    status IN ('PUBLISHED', 'PROCESSING', 'COMPLETED', 'FAILED', 'RESUBMITTED')
  ),
  PRIMARY KEY (id)
);
