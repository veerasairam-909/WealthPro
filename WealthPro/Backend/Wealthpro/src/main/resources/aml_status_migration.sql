-- AML Status Migration: collapse to 3 statuses (OPEN, REVIEWED, CLOSED)
-- Run this ONCE against the kyc database before restarting the Wealthpro service.

UPDATE AmlFlag SET Status = 'REVIEWED'  WHERE Status = 'ESCALATED';
UPDATE AmlFlag SET Status = 'CLOSED'    WHERE Status = 'PENDING_CLOSURE';
UPDATE AmlFlag SET Status = 'CLOSED'    WHERE Status = 'CLEARED';
