-- =====================================================================
-- MyDocx — Supabase schema
-- Run this once in the Supabase SQL editor (or via `supabase db push`).
-- =====================================================================

-- ---------- Extensions ----------
create extension if not exists "uuid-ossp";

-- ---------- profiles ----------
-- One row per auth.users row (id matches auth.users.id 1:1).
create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    username text unique not null,
    full_name text not null,
    bio text,
    avatar_url text,
    avatar_seed text,               -- seeds the client-side generated default avatar
    special_thanks text,
    social_links jsonb default '[]'::jsonb,   -- [{ "label": "GitHub", "url": "https://..." }]
    pin_hash text,                  -- hashed 4-digit "red passkey" PIN (see util/PinHasher.kt)
    pin_salt text,
    followers_count int not null default 0,
    following_count int not null default 0,
    created_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

create policy "Profiles are viewable by everyone"
    on public.profiles for select using (true);

create policy "Users can update their own profile"
    on public.profiles for update using (auth.uid() = id);

-- Auto-create a profile row whenever a new auth user signs up.
-- full_name / username are passed in the signUp() `data` payload from the app.
create or replace function public.handle_new_user()
returns trigger as $$
begin
    insert into public.profiles (id, username, full_name, avatar_seed)
    values (
        new.id,
        coalesce(new.raw_user_meta_data->>'username', split_part(new.email, '@', 1)),
        coalesce(new.raw_user_meta_data->>'full_name', 'مستخدم جديد'),
        coalesce(new.raw_user_meta_data->>'username', new.email)
    );
    return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute procedure public.handle_new_user();

-- ---------- articles ----------
create table if not exists public.articles (
    id uuid primary key default uuid_generate_v4(),
    author_id uuid not null references public.profiles(id) on delete cascade,
    title text not null,
    body text not null,
    cover_url text,
    likes_count int not null default 0,
    reposts_count int not null default 0,
    created_at timestamptz not null default now()
);

alter table public.articles enable row level security;

create policy "Articles are viewable by everyone"
    on public.articles for select using (true);

create policy "Users can insert their own articles"
    on public.articles for insert with check (auth.uid() = author_id);

create policy "Users can update/delete their own articles"
    on public.articles for update using (auth.uid() = author_id);

create policy "Users can delete their own articles"
    on public.articles for delete using (auth.uid() = author_id);

-- ---------- projects ----------
create table if not exists public.projects (
    id uuid primary key default uuid_generate_v4(),
    author_id uuid not null references public.profiles(id) on delete cascade,
    name text not null,                 -- e.g. "MyProject.docx"
    description text not null,
    repo_url text,
    storage_path text,                  -- public URL of the structured .zip in Supabase Storage
    language text,
    likes_count int not null default 0,
    reposts_count int not null default 0,
    created_at timestamptz not null default now()
);

alter table public.projects enable row level security;

create policy "Projects are viewable by everyone"
    on public.projects for select using (true);

create policy "Users can insert their own projects"
    on public.projects for insert with check (auth.uid() = author_id);

create policy "Users can update their own projects"
    on public.projects for update using (auth.uid() = author_id);

create policy "Users can delete their own projects"
    on public.projects for delete using (auth.uid() = author_id);

-- ---------- likes ----------
create table if not exists public.likes (
    user_id uuid not null references public.profiles(id) on delete cascade,
    content_type text not null check (content_type in ('article', 'project')),
    content_id uuid not null,
    created_at timestamptz not null default now(),
    primary key (user_id, content_type, content_id)
);

alter table public.likes enable row level security;

create policy "Likes are viewable by everyone"
    on public.likes for select using (true);

create policy "Users manage their own likes"
    on public.likes for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

-- Keep likes_count in sync automatically.
create or replace function public.handle_like_change()
returns trigger as $$
begin
    if (tg_op = 'INSERT') then
        if new.content_type = 'article' then
            update public.articles set likes_count = likes_count + 1 where id = new.content_id;
        else
            update public.projects set likes_count = likes_count + 1 where id = new.content_id;
        end if;
        return new;
    elsif (tg_op = 'DELETE') then
        if old.content_type = 'article' then
            update public.articles set likes_count = greatest(likes_count - 1, 0) where id = old.content_id;
        else
            update public.projects set likes_count = greatest(likes_count - 1, 0) where id = old.content_id;
        end if;
        return old;
    end if;
    return null;
end;
$$ language plpgsql security definer;

drop trigger if exists on_like_change on public.likes;
create trigger on_like_change
    after insert or delete on public.likes
    for each row execute procedure public.handle_like_change();

-- ---------- reposts ----------
create table if not exists public.reposts (
    user_id uuid not null references public.profiles(id) on delete cascade,
    content_type text not null check (content_type in ('article', 'project')),
    content_id uuid not null,
    created_at timestamptz not null default now(),
    primary key (user_id, content_type, content_id)
);

alter table public.reposts enable row level security;

create policy "Reposts are viewable by everyone"
    on public.reposts for select using (true);

create policy "Users manage their own reposts"
    on public.reposts for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create or replace function public.handle_repost_change()
returns trigger as $$
begin
    if (tg_op = 'INSERT') then
        if new.content_type = 'article' then
            update public.articles set reposts_count = reposts_count + 1 where id = new.content_id;
        else
            update public.projects set reposts_count = reposts_count + 1 where id = new.content_id;
        end if;
        return new;
    elsif (tg_op = 'DELETE') then
        if old.content_type = 'article' then
            update public.articles set reposts_count = greatest(reposts_count - 1, 0) where id = old.content_id;
        else
            update public.projects set reposts_count = greatest(reposts_count - 1, 0) where id = old.content_id;
        end if;
        return old;
    end if;
    return null;
end;
$$ language plpgsql security definer;

drop trigger if exists on_repost_change on public.reposts;
create trigger on_repost_change
    after insert or delete on public.reposts
    for each row execute procedure public.handle_repost_change();

-- ---------- reports ----------
create table if not exists public.reports (
    id uuid primary key default uuid_generate_v4(),
    reporter_id uuid not null references public.profiles(id) on delete cascade,
    content_type text not null check (content_type in ('article', 'project')),
    content_id uuid not null,
    reason text not null,
    status text not null default 'pending' check (status in ('pending', 'reviewed', 'dismissed')),
    created_at timestamptz not null default now()
);

alter table public.reports enable row level security;

create policy "Users can insert their own reports"
    on public.reports for insert with check (auth.uid() = reporter_id);

create policy "Users can view their own submitted reports"
    on public.reports for select using (auth.uid() = reporter_id);
-- Moderators/admins should review reports via the Supabase dashboard or the
-- service_role key (server-side only) — never expose report visibility to all users.

-- ---------- follows ----------
create table if not exists public.follows (
    follower_id uuid not null references public.profiles(id) on delete cascade,
    following_id uuid not null references public.profiles(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (follower_id, following_id),
    check (follower_id <> following_id)
);

alter table public.follows enable row level security;

create policy "Follows are viewable by everyone"
    on public.follows for select using (true);

create policy "Users manage their own follow edges"
    on public.follows for all using (auth.uid() = follower_id) with check (auth.uid() = follower_id);

create or replace function public.handle_follow_change()
returns trigger as $$
begin
    if (tg_op = 'INSERT') then
        update public.profiles set following_count = following_count + 1 where id = new.follower_id;
        update public.profiles set followers_count = followers_count + 1 where id = new.following_id;
        return new;
    elsif (tg_op = 'DELETE') then
        update public.profiles set following_count = greatest(following_count - 1, 0) where id = old.follower_id;
        update public.profiles set followers_count = greatest(followers_count - 1, 0) where id = old.following_id;
        return old;
    end if;
    return null;
end;
$$ language plpgsql security definer;

drop trigger if exists on_follow_change on public.follows;
create trigger on_follow_change
    after insert or delete on public.follows
    for each row execute procedure public.handle_follow_change();

-- ---------- newsletter_subscriptions (monthly email subscription) ----------
create table if not exists public.newsletter_subscriptions (
    email text primary key,
    user_id uuid references public.profiles(id) on delete set null,
    active boolean not null default true,
    created_at timestamptz not null default now()
);

alter table public.newsletter_subscriptions enable row level security;

create policy "Anyone can subscribe"
    on public.newsletter_subscriptions for insert with check (true);

create policy "Users can view/update their own subscription"
    on public.newsletter_subscriptions for select using (auth.uid() = user_id or user_id is null);

create policy "Users can update their own subscription"
    on public.newsletter_subscriptions for update using (auth.uid() = user_id);

-- Actually sending the monthly email is done server-side by the
-- `send-monthly-newsletter` Edge Function (see supabase/edge-functions/),
-- invoked on a schedule via Supabase's pg_cron / Scheduled Functions —
-- never from the Android client, since that requires the service_role key.

-- =====================================================================
-- Storage buckets: run in the Supabase dashboard (Storage) or via SQL:
--   insert into storage.buckets (id, name, public) values ('avatars', 'avatars', true);
--   insert into storage.buckets (id, name, public) values ('projects', 'projects', true);
-- =====================================================================
insert into storage.buckets (id, name, public)
values ('avatars', 'avatars', true)
on conflict (id) do nothing;

insert into storage.buckets (id, name, public)
values ('projects', 'projects', true)
on conflict (id) do nothing;

create policy "Avatar images are publicly accessible"
    on storage.objects for select using (bucket_id = 'avatars');

-- The app uploads avatars as "{user_id}.jpg" directly in the bucket root
-- (see StorageUploader.upload(bucket="avatars", path="$myId.jpg", ...)),
-- so a signed-in user may only overwrite the object that matches their own id.
create policy "Users can upload their own avatar"
    on storage.objects for insert with check (
        bucket_id = 'avatars'
        and auth.role() = 'authenticated'
        and name = auth.uid()::text || '.jpg'
    );

create policy "Users can update their own avatar"
    on storage.objects for update using (
        bucket_id = 'avatars'
        and auth.role() = 'authenticated'
        and name = auth.uid()::text || '.jpg'
    );

create policy "Project zips are publicly accessible"
    on storage.objects for select using (bucket_id = 'projects');

-- The app uploads project zips under "{user_id}/{timestamp}_{name}.zip"
-- (see PublishProjectActivity), so restrict writes to the caller's own folder.
create policy "Users can upload their own project files"
    on storage.objects for insert with check (
        bucket_id = 'projects'
        and auth.role() = 'authenticated'
        and (storage.foldername(name))[1] = auth.uid()::text
    );
