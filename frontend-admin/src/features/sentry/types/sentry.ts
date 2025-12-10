import { z } from 'zod';

export const SentryIssueSchema = z.object({
  id: z.string(),
  shortId: z.string(),
  title: z.string(),
  culprit: z.string(),
  level: z.enum(['error', 'warning', 'info', 'debug', 'fatal']),
  status: z.enum(['unresolved', 'resolved', 'ignored', 'muted']),
  count: z.number(),
  userCount: z.number(),
  firstSeen: z.string(),
  lastSeen: z.string(),
  permalink: z.string(),
  assignedTo: z
    .object({
      id: z.string(),
      name: z.string(),
      email: z.string(),
    })
    .nullable()
    .optional(),
  metadata: z
    .object({
      type: z.string().optional(),
      value: z.string().optional(),
      filename: z.string().optional(),
      function: z.string().optional(),
    })
    .optional(),
});

export const SentryEventSchema = z.object({
  id: z.string(),
  issue: z.string(),
  message: z.string().optional(),
  level: z.enum(['error', 'warning', 'info', 'debug', 'fatal']),
  platform: z.string(),
  timestamp: z.string(),
  tags: z.record(z.string(), z.string()).optional(),
  user: z
    .object({
      id: z.string().optional(),
      username: z.string().optional(),
      email: z.string().optional(),
      ip_address: z.string().optional(),
    })
    .optional()
    .nullable(),
  contexts: z
    .object({
      browser: z
        .object({
          name: z.string().optional(),
          version: z.string().optional(),
        })
        .optional(),
      os: z
        .object({
          name: z.string().optional(),
          version: z.string().optional(),
        })
        .optional(),
      device: z
        .object({
          name: z.string().optional(),
          model: z.string().optional(),
        })
        .optional(),
    })
    .optional(),
  breadcrumbs: z
    .array(
      z.object({
        timestamp: z.number(),
        category: z.string(),
        message: z.string().optional(),
        level: z.string().optional(),
        data: z.record(z.string(), z.unknown()).optional(),
      }),
    )
    .optional(),
  exception: z
    .object({
      values: z.array(
        z.object({
          type: z.string(),
          value: z.string(),
          stacktrace: z
            .object({
              frames: z.array(
                z.object({
                  filename: z.string().optional(),
                  function: z.string().optional(),
                  lineno: z.number().optional(),
                  colno: z.number().optional(),
                  context_line: z.string().optional(),
                  pre_context: z.array(z.string()).optional(),
                  post_context: z.array(z.string()).optional(),
                }),
              ),
            })
            .optional(),
        }),
      ),
    })
    .optional(),
});

export type SentryIssue = z.infer<typeof SentryIssueSchema>;
export type SentryEvent = z.infer<typeof SentryEventSchema>;

export interface SentryLogsResponse {
  issues: SentryIssue[];
  total: number;
  page: number;
  pageSize: number;
}

export interface SentryEventResponse {
  event: SentryEvent;
}
