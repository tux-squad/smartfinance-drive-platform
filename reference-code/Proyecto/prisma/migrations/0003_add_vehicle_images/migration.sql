alter table public.vehicles
  add column if not exists image_path text;

insert into storage.buckets (
  id,
  name,
  public,
  file_size_limit,
  allowed_mime_types
)
values (
  'vehicle-images',
  'vehicle-images',
  true,
  3145728,
  array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update
set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

do $$
begin
  if not exists (
    select 1 from pg_policies
    where schemaname = 'storage'
      and tablename = 'objects'
      and policyname = 'Vehicle images are publicly readable'
  ) then
    create policy "Vehicle images are publicly readable"
      on storage.objects for select
      to public
      using (bucket_id = 'vehicle-images');
  end if;

  if not exists (
    select 1 from pg_policies
    where schemaname = 'storage'
      and tablename = 'objects'
      and policyname = 'Vehicle images are insertable by owner'
  ) then
    create policy "Vehicle images are insertable by owner"
      on storage.objects for insert
      to authenticated
      with check (
        bucket_id = 'vehicle-images'
        and (storage.foldername(name))[1] = (select auth.uid())::text
      );
  end if;

  if not exists (
    select 1 from pg_policies
    where schemaname = 'storage'
      and tablename = 'objects'
      and policyname = 'Vehicle images are updatable by owner'
  ) then
    create policy "Vehicle images are updatable by owner"
      on storage.objects for update
      to authenticated
      using (
        bucket_id = 'vehicle-images'
        and (storage.foldername(name))[1] = (select auth.uid())::text
      )
      with check (
        bucket_id = 'vehicle-images'
        and (storage.foldername(name))[1] = (select auth.uid())::text
      );
  end if;

  if not exists (
    select 1 from pg_policies
    where schemaname = 'storage'
      and tablename = 'objects'
      and policyname = 'Vehicle images are deletable by owner'
  ) then
    create policy "Vehicle images are deletable by owner"
      on storage.objects for delete
      to authenticated
      using (
        bucket_id = 'vehicle-images'
        and (storage.foldername(name))[1] = (select auth.uid())::text
      );
  end if;
end $$;

notify pgrst, 'reload schema';
